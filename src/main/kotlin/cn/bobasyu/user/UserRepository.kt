package cn.bobasyu.user

import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import io.vertx.core.eventbus.Message


/**
 * AbstractUserRepository使用vertx-mysql的具体实现
 */
class UserRepositoryVerticle(
    private val mySqlClient: MySqlClient,
) : AbstractUserRepository() {

    override suspend fun handleQueryUserListEvent(message: Message<Unit>) {
        val queryListSql: String = SqlGenerator(UserRecord::class).select().generate()
        mySqlClient.query(queryListSql, UserRecord::class.java)
            .onSuccess { userList: List<UserRecord> -> message.reply(userList) }
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleQueryUserByIdEvent(message: Message<Int>) {
        SqlGenerator(UserRecord::class).select()
            .where().eq(UserRecord::userId, message.body())
            .execute(mySqlClient)
            .onSuccess { userRecordList ->
                if ((userRecordList as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException()
                }
                message.reply(userRecordList.first())
            }.onFailure { message.fail(500, it.message) }

    }

    override suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>) {
        val userInsertDTO: UserInsertDTO = message.body()
        SqlGenerator(UserRecord::class)
            .insert(UserRecord::username, UserRecord::password)
            .values(userInsertDTO.username, userInsertDTO.password)
            .execute(mySqlClient)
            .onFailure { message.fail(500, it.message) }

    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) {
        val userLoginDTO: UserLoginDTO = message.body()
        SqlGenerator(UserRecord::class).select()
            .where().eq(UserRecord::username, userLoginDTO.userName)
            .and().eq(UserRecord::password, userLoginDTO.password)
            .execute(mySqlClient)
            .onSuccess { userList ->
                if ((userList as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException()
                }
            }.onFailure { message.fail(500, it.message) }

    }
}
