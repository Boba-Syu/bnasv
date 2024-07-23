package cn.bobasyu.user

import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.user.UserSQL.INSERT_SQL
import cn.bobasyu.user.UserSQL.QUERY_BY_ID_SQL
import cn.bobasyu.user.UserSQL.QUERY_BY_USERNAME_AND_PASSWORD_SQL
import cn.bobasyu.user.UserSQL.QUERY_LIST_SQL
import io.vertx.core.eventbus.Message
import io.vertx.sqlclient.Tuple

object UserSQL {
    const val QUERY_LIST_SQL = "select * from db_user"
    const val QUERY_BY_ID_SQL = "select * from db_user where user_id = ?"
    const val QUERY_BY_USERNAME_AND_PASSWORD_SQL = "select * from db_user where username = ? and password = ?"
    const val INSERT_SQL = "insert into db_user (user_name, password) values(?, ?)"
}

class UserRepositoryVerticle(
    private val mySqlClient: MySqlClient,
) : AbstractUserRepository() {

    override suspend fun handleQueryUserListEvent(message: Message<Unit>) {
        mySqlClient.query(QUERY_LIST_SQL, UserRecord::class.java)
            .onSuccess { userList: List<UserRecord> -> message.reply(userList) }
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleQueryUserByIdEvent(message: Message<Int>) {
        val condition: List<Tuple> = listOf(Tuple.of(message.body()))
        mySqlClient.queryByConditions(QUERY_BY_ID_SQL, condition, UserRecord::class.java)
            .onSuccess { userRecordList: List<UserRecord> ->
                if (userRecordList.isEmpty()) {

                }
                message.reply(userRecordList.first())
            }
            .onFailure { message.fail(500, it.message) }
    }

    override fun handleInsertUserEvent(message: Message<UserInsertDTO>) {
        val userInsertDTO: UserInsertDTO = message.body()
        val condition: List<Tuple> = listOf(
            Tuple.of(userInsertDTO.userName, userInsertDTO.password)
        )
        mySqlClient.save(INSERT_SQL, condition)
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) {
        val userLoginDTO: UserLoginDTO = message.body()
        val condition: List<Tuple> = listOf(
            Tuple.of(userLoginDTO.userName, userLoginDTO.password)
        )
        mySqlClient.queryByConditions(QUERY_BY_USERNAME_AND_PASSWORD_SQL, condition, UserRecord::class.java)
            .onSuccess { userList: List<UserRecord> ->
                if (userList.isEmpty()) {
                    throw NoSuchRecordInDatabaseException()
                }
            }.onFailure { message.fail(500, it.message) }
    }
}
