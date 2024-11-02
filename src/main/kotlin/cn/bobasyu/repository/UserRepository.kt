package cn.bobasyu.repository

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import cn.bobasyu.user.UserInsertDTO
import cn.bobasyu.user.UserLoginDTO
import cn.bobasyu.user.UserRecord
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.await


/**
 * 用户操作Repository抽象类，消费相关总线事件返回数据库操作结果，抽离出数据库操作的具体实现，方便日后更换底层实现
 */
abstract class AbstractUserRepository(
    applicationContext: ApplicationContext
) : BaseCoroutineVerticle(applicationContext) {
    private val eventBus: EventBus by lazy { vertx.eventBus() }

    override suspend fun start() {
        registerConsumer()
    }

    /**
     * 注册总线事件消费方法
     */
    fun registerConsumer() = with(eventBus) {
        asyncConsumer(USER_QUERY_EVENT) { handleQueryUserListEvent(it) }
        asyncConsumer(USER_QUERY_BY_ID_EVENT) { handleQueryUserByIdEvent(it) }
        asyncConsumer(USER_INSERT_EVENT) { handleInsertUserEvent(it) }
        asyncConsumer(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT) { handleQueryUserByUsernameAndPasswordEvent(it) }
    }

    abstract suspend fun handleQueryUserListEvent(message: Message<Unit>)
    abstract suspend fun handleQueryUserByIdEvent(message: Message<Int>)
    abstract suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>)
    abstract suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>)
}


/**
 * AbstractUserRepository使用vertx-mysql的具体实现
 */
open class UserRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractUserRepository(applicationContext) {
    private val mySqlClient: MySqlClient = applicationContext.mySqlClient

    override suspend fun start() {
        super.start()
    }

    override suspend fun handleQueryUserListEvent(message: Message<Unit>) {
        queryUserList()
            .onSuccess { userList: List<UserRecord> -> message.reply(userList) }
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleQueryUserByIdEvent(message: Message<Int>) {
        val userId = message.body()
        queryUserById(userId)
            .onSuccess { message.reply(it as UserRecord) }
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>) {
        val userInsertDTO: UserInsertDTO = message.body()
        val ifExisted = queryUsernameExist(userInsertDTO.username).await()
        if (ifExisted) {
            throw BaseException(message = "username${userInsertDTO.username} is existed")
        }

        insertUser(userInsertDTO)
            .onSuccess { message.reply("success") }
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) {
        val userLoginDTO: UserLoginDTO = message.body()
        queryUserByUsernameAndPassword(userLoginDTO)
            .onSuccess { userList -> message.reply(userList) }
            .onFailure { message.fail(500, it.message) }
    }

    private fun queryUsernameExist(username: String): Future<Boolean> {
        return SqlGenerator(UserRecord::class)
            .select()
            .where().eq(UserRecord::username, username)
            .execute(mySqlClient)
            .map { return@map (it as List<*>).isNotEmpty() }
    }

    private fun queryUserList(): Future<List<UserRecord>> {
        val queryListSql: String = SqlGenerator(UserRecord::class).select().generate()
        return mySqlClient.query(queryListSql, UserRecord::class.java)
    }

    private fun queryUserById(id: Int): Future<UserRecord> {
        return SqlGenerator(UserRecord::class)
            .select()
            .where().eq(UserRecord::userId, id)
            .execute(mySqlClient)
            .map {
                if ((it as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException("id: $id")
                }
                it.first()
            }
            .map { it as UserRecord }
    }

    private fun queryUserByUsername(username: String): Future<UserRecord> {
        return SqlGenerator(UserRecord::class)
            .select()
            .where().eq(UserRecord::username, username)
            .execute(mySqlClient)
            .map {
                if ((it as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException("username: $username")
                }
                it.first()
            }
            .map { it as UserRecord }
    }

    private fun insertUser(userInsertDTO: UserInsertDTO): Future<Unit> {
        return SqlGenerator(UserRecord::class)
            .insert(UserRecord::username, UserRecord::password)
            .values(userInsertDTO.username, userInsertDTO.password)
            .execute(mySqlClient)
            .map {}
    }

    private fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): Future<UserRecord> {
        return SqlGenerator(UserRecord::class).select()
            .where().eq(UserRecord::username, userLoginDTO.username)
            .and().eq(UserRecord::password, userLoginDTO.password)
            .execute(mySqlClient)
            .map {
                if ((it as List<*>).isEmpty()) {
                    throw BaseException(message = "username or password error")
                }
                it.first()
            }.map { it as UserRecord }
    }
}
