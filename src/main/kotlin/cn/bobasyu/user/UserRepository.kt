package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.await


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

    override suspend fun queryUserList(): Future<List<UserRecord>> {
        val queryListSql: String = SqlGenerator(UserRecord::class).select().generate()
        return mySqlClient.query(queryListSql, UserRecord::class.java)
    }

    override suspend fun queryUserById(id: Int): Future<UserRecord> {
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

    override suspend fun queryUserByUsername(username: String): Future<UserRecord> {
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


    override suspend fun insertUser(userInsertDTO: UserInsertDTO): Future<Unit> {
        return SqlGenerator(UserRecord::class)
            .insert(UserRecord::username, UserRecord::password)
            .values(userInsertDTO.username, userInsertDTO.password)
            .execute(mySqlClient)
            .map {}
    }


    override suspend fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): Future<UserRecord> {
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
