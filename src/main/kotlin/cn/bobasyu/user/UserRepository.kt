package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.SqlClient
import cn.bobasyu.entity.UserInsertDTO
import cn.bobasyu.entity.UserLoginDTO
import cn.bobasyu.entity.UserRecord
import io.vertx.core.eventbus.Message


/**
 * AbstractUserRepository使用vertx-mysql的具体实现
 */
open class UserRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractUserRepository(applicationContext) {

    private val sqlClient: SqlClient = applicationContext.sqlClient

    override suspend fun start() {
        super.start()
    }

    override suspend fun handleQueryUserListEvent(message: Message<Unit>) = handleEvent(message) {
        queryUserList()
    }

    override suspend fun handleQueryUserByIdEvent(message: Message<Int>) = handleEvent(message) {
        val userId: Int = message.body()
        queryUserById(userId)
    }

    override suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>) = handleEvent(message) {
        val userInsertDTO: UserInsertDTO = message.body()
        val ifExisted = queryUsernameExist(userInsertDTO.username)
        if (ifExisted) {
            throw BaseException(message = "username${userInsertDTO.username} is existed")
        }
        insertUser(userInsertDTO)
    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) = handleEvent(message) {
            val userLoginDTO: UserLoginDTO = message.body()
            queryUserByUsernameAndPassword(userLoginDTO)
        }

    private fun queryUsernameExist(username: String): Boolean {
        val list: List<UserRecord> = sqlClient.withSession { session ->
            session.createQuery("FROM UserRecord where username = :username", UserRecord::class.java)
                .setParameter(0, username)
                .resultList
        }.await().indefinitely()
        return list.isNotEmpty()
    }

    private fun queryUserList(): List<UserRecord> {
        return sqlClient.withSession { session ->
            session.find(UserRecord::class.java)
        }.await().indefinitely()
    }

    private fun queryUserById(id: Int): UserRecord {
        val userRecord: UserRecord? = sqlClient.withSession { session ->
            session.find(UserRecord::class.java, id)
        }.await().indefinitely()
        if (userRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return userRecord
    }

    private fun queryUserByUsername(username: String): UserRecord {
        val list: List<UserRecord> = sqlClient.withSession { session ->
            session.createQuery("FROM UserRecord where username = :username", UserRecord::class.java)
                .setParameter(0, username)
                .resultList
        }.await().indefinitely()
        if (list.isEmpty()) {
            throw NoSuchRecordInDatabaseException("username: $username")
        }
        return list.first()

    }

    private fun insertUser(userInsertDTO: UserInsertDTO): Unit {
        sqlClient.withSession { session ->
            val userRecord = UserRecord(username = userInsertDTO.username, password = userInsertDTO.password)
            session.persist(userRecord)
        }.await().indefinitely()
    }

    private fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): UserRecord {
        val list: List<UserRecord> = sqlClient.withSession { session ->
            session.createQuery(
                "FROM UserRecord WHERE username = :username AND password = :password",
                UserRecord::class.java
            )
                .setParameter(0, userLoginDTO.username)
                .setParameter(1, userLoginDTO.password)
                .resultList
        }.await().indefinitely()
        if (list.isEmpty()) {
            throw BaseException(message = "username or password error")
        }
        return list.first()
    }
}
