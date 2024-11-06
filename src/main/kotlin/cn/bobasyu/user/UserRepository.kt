package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.PostgresqlClient
import cn.bobasyu.databeses.SqlGenerator
import cn.bobasyu.entity.UserInsertDTO
import cn.bobasyu.entity.UserLoginDTO
import cn.bobasyu.entity.UserRecord
import io.vertx.core.Future
import io.vertx.core.eventbus.Message


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

    override suspend fun handleQueryUserByIdEvent(message: Message<Int>) = handle(message) {
        val userId: Int = message.body()
        queryUserById(userId)
    }

    override suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>)  = handle(message) {
        val userInsertDTO: UserInsertDTO = message.body()
        val ifExisted = queryUsernameExist(userInsertDTO.username)
        if (ifExisted) {
            throw BaseException(message = "username${userInsertDTO.username} is existed")
        }
        insertUser(userInsertDTO)
    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) = handle(message) {
        val userLoginDTO: UserLoginDTO = message.body()
        queryUserByUsernameAndPassword(userLoginDTO)
    }

    private fun queryUsernameExist(username: String): Boolean {
        val list: List<UserRecord> = PostgresqlClient.withSession { session ->
            session.createQuery("FROM UserRecord where username = :username", UserRecord::class.java)
                .setParameter(0, username)
                .resultList
        }.await().indefinitely()
        return list.isNotEmpty()
    }

    private fun queryUserList(): Future<List<UserRecord>> {
        val queryListSql: String = SqlGenerator(UserRecord::class).select().generate()
        return mySqlClient.query(queryListSql, UserRecord::class.java)
    }

    private fun queryUserById(id: Int): UserRecord {
        val userRecord: UserRecord? = PostgresqlClient.withSession { session ->
            session.find(UserRecord::class.java, id)
        }.await().indefinitely()
        if (userRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return userRecord
    }

    private fun queryUserByUsername(username: String): UserRecord {
        val list: List<UserRecord> = PostgresqlClient.withSession { session ->
            session.createQuery("FROM UserRecord where username = :username", UserRecord::class.java)
                .setParameter(0, username)
                .resultList
        }.await().indefinitely()
        if (list.isEmpty()) {
            throw NoSuchRecordInDatabaseException("username: $username")
        }
        return list.first()

    }

    private fun insertUser(userInsertDTO: UserInsertDTO) : Unit {
        PostgresqlClient.withSession { session ->
            val userRecord = UserRecord(username = userInsertDTO.username, password = userInsertDTO.password)
            session.persist(userRecord)
        }.await().indefinitely()
    }

    private fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): UserRecord {
        val list: List<UserRecord> = PostgresqlClient.withSession { session ->
            session.createQuery("FROM UserRecord WHERE username = :username AND password = :password", UserRecord::class.java)
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
