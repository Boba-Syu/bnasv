package cn.bobasyu.user

import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import cn.bobasyu.user.UserSQL.INSERT_SQL
import cn.bobasyu.user.UserSQL.QUERY_BY_ID_SQL
import cn.bobasyu.user.UserSQL.QUERY_LIST_SQL
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Tuple

object UserSQL {
    const val QUERY_LIST_SQL = "select * from db_user"
    const val QUERY_BY_ID_SQL = "select * from db_user where user_id = ?"
    const val INSERT_SQL = "insert into db_user (user_name, user_password) values(?, ?)"
}

class UserRepositoryVerticle(
    private val mySqlClient: MySqlClient,
) : BaseCoroutineVerticle() {

    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }

    override suspend fun start() {
        registerConsumer()
    }

    private suspend fun registerConsumer() = with(eventBus) {
        registerQueryUserListEvent()
        registerQueryUserByIdEvent()
        registerInsertUserEvent()
    }

    private suspend fun EventBus.registerQueryUserListEvent() {
        asyncConsumer(USER_QUERY_EVENT) { message: Message<Unit> ->
            val userList: List<UserRecord> = mySqlClient.query(QUERY_LIST_SQL, UserRecord::class.java).await()
            message.reply(userList)
        }
    }

    private suspend fun EventBus.registerQueryUserByIdEvent() {
        asyncConsumer(USER_QUERY_BY_ID_EVENT) { message: Message<Int> ->
            val condition: List<Tuple> = listOf(Tuple.of(message.body()))
            val userRecord: UserRecord =
                mySqlClient.queryByConditions(QUERY_BY_ID_SQL, condition, UserRecord::class.java).await()
            message.reply(userRecord)
        }
    }

    private suspend fun EventBus.registerInsertUserEvent() {
        asyncConsumer(USER_INSERT_EVENT) { message: Message<InsertUserDto> ->
            val insertUserDto: InsertUserDto = message.body()
            val condition: List<Tuple> = listOf(
                Tuple.of(insertUserDto.userName, insertUserDto.userPassword)
            )
            mySqlClient.save(INSERT_SQL, condition)
                .onFailure { message.fail(500, it.message) }
        }
    }
}
