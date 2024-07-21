package cn.bobasyu.user

import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import cn.bobasyu.user.UserSQL.QUERY_BY_ID_SQL
import cn.bobasyu.user.UserSQL.QUERY_LIST_SQL
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Tuple

object UserSQL {
    const val QUERY_LIST_SQL = "select * from db_user"
    const val QUERY_BY_ID_SQL = "select * from db_user where user_id = ?"
}

class UserRepositoryVerticle(
    private val mySqlClient: MySqlClient,
) : BaseRepositoryVerticle() {

    override suspend fun start() {
        registerConsumer()
    }

    private suspend fun registerConsumer() {
        val eventBus: EventBus = vertx.eventBus()
            .registerDefaultCodec(User::class.java, UserCodec())
        eventBus.registerQueryUserListEvent()
        eventBus.registerQueryUserByIdEvent()
    }

    private suspend fun EventBus.registerQueryUserListEvent() {
        asyncConsumer<Unit>(USER_QUERY_EVENT) { message ->
            val userList: List<User> = mySqlClient.query(QUERY_LIST_SQL, User::class.java).await()
            message.reply(userList)
        }
    }

    private suspend fun EventBus.registerQueryUserByIdEvent() {
        asyncConsumer<Int>(USER_QUERY_BY_ID_EVENT) { message ->
            val condition = Tuple.of(message.body() as Int)
            val user = mySqlClient.queryByCondition(QUERY_BY_ID_SQL, condition, User::class.java).await()
            message.reply(user)
        }
    }
}