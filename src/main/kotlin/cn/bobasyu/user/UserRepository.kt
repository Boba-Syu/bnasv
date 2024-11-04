package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import cn.bobasyu.entity.UserInsertDTO
import cn.bobasyu.entity.UserLoginDTO
import cn.bobasyu.entity.UserRecord
import cn.bobasyu.utils.camelToSnakeCase
import cn.bobasyu.utils.toJson
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.await
import sun.jvm.hotspot.HelloWorld.e
import javax.persistence.EntityManager
import javax.persistence.Persistence


/**
 * AbstractUserRepository使用vertx-mysql的具体实现
 */
open class UserRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractUserRepository(applicationContext) {
    private val mySqlClient: MySqlClient = applicationContext.mySqlClient

    val entityManager: EntityManager by lazy {
        val factory = Persistence.createEntityManagerFactory("bnasv-hibernate")
        factory.createEntityManager()
    }

    override suspend fun start() {
        super.start()
    }

    override suspend fun handleQueryUserListEvent(message: Message<Unit>) {
        val userId = message.body()
        try {
            val userRecordList = queryUserList()
            message.reply(userRecordList)
        } catch (e: Exception) {
            message.fail(500, e.message)
        }
    }

    override suspend fun handleQueryUserByIdEvent(message: Message<Int>) {
        val userId = message.body()
        try {
            val userRecord = queryUserById(userId)
            message.reply(userRecord)
        } catch (e: Exception) {
            message.fail(500, e.message)
        }
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
        val userList = queryUserByUsernameAndPassword(userLoginDTO)
        message.reply(userList)
    }

    private fun queryUsernameExist(username: String): Future<Boolean> {
        return SqlGenerator(UserRecord::class)
            .select()
            .where().eq(UserRecord::username, username)
            .execute(mySqlClient)
            .map { return@map (it as List<*>).isNotEmpty() }
    }

    private fun queryUserList(): List<UserRecord> {
        val tableName = UserRecord::class.simpleName!!.camelToSnakeCase()
        return entityManager.createQuery(tableName, UserRecord::class.java).resultList
    }

    private fun queryUserById(id: Int): UserRecord {
        val userRecord: UserRecord? = entityManager.find(UserRecord::class.java, id)
        if (userRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return userRecord

    }

    private fun queryUserByUsername(username: String): UserRecord {
        val sql = "SELECT * FROM ${UserRecord::class.simpleName!!.camelToSnakeCase()} WHERE username = :username"
        val query = entityManager.createQuery(sql, UserRecord::class.java)
        query.setParameter("username", username)
        val userRecordList: List<UserRecord> = query.resultList
        if (userRecordList.isEmpty()) {
            throw NoSuchRecordInDatabaseException("username: $username")
        }
        return userRecordList.first()
    }

    private fun insertUser(userInsertDTO: UserInsertDTO): Future<Unit> {
        return SqlGenerator(UserRecord::class)
            .insert(UserRecord::username, UserRecord::password)
            .values(userInsertDTO.username, userInsertDTO.password)
            .execute(mySqlClient)
            .map {}
    }

    private fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): UserRecord {
        val sql = "SELECT * FROM ${UserRecord::class.simpleName!!.camelToSnakeCase()} " +
                "WHERE username = :username"
        val query = entityManager.createQuery(sql, UserRecord::class.java)
        query.setParameter("username", userLoginDTO.username)
        val userRecordList: List<UserRecord> = query.resultList
        if (userRecordList.isEmpty()) {
            throw NoSuchRecordInDatabaseException("UserLoginDTO: ${userLoginDTO.toJson()}")
        }
        return userRecordList.first()
    }
}
