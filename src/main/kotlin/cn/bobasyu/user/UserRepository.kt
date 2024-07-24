package cn.bobasyu.user

import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import io.vertx.core.eventbus.Message
import io.vertx.sqlclient.Tuple


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
        val queryByUserIdSql:String = SqlGenerator(UserRecord::class).select()
            .where().eq(UserRecord::userId)
            .generate()
        val condition: List<Tuple> = listOf(Tuple.of(message.body()))
        mySqlClient.queryByConditions(queryByUserIdSql, condition, UserRecord::class.java)
            .onSuccess { userRecordList: List<UserRecord> ->
                if (userRecordList.isEmpty()) {
                    throw NoSuchRecordInDatabaseException()
                }
                message.reply(userRecordList.first())
            }.onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>) {
        val userInsertDTO: UserInsertDTO = message.body()
        val insertSql: String = SqlGenerator(UserRecord::class)
            .insert(UserRecord::username, UserRecord::password)
            .generate()
        val condition: List<Tuple> = listOf(
            Tuple.of(userInsertDTO.username, userInsertDTO.password)
        )
        mySqlClient.save(insertSql, condition)
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) {
        val userLoginDTO: UserLoginDTO = message.body()
        val queryUserByUsernameAndPasswordSql:String = SqlGenerator(UserRecord::class).select()
            .where().eq(UserRecord::username)
            .and().eq(UserRecord::password)
            .generate()
        val condition: List<Tuple> = listOf(
            Tuple.of(userLoginDTO.userName, userLoginDTO.password)
        )
        mySqlClient.queryByConditions(queryUserByUsernameAndPasswordSql, condition, UserRecord::class.java)
            .onSuccess { userList: List<UserRecord> ->
                if (userList.isEmpty()) {
                    throw NoSuchRecordInDatabaseException()
                }
            }.onFailure { message.fail(500, it.message) }
    }
}
