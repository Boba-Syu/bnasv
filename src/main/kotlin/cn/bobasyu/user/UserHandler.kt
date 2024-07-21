package cn.bobasyu.user

import cn.bobasyu.base.BaseServiceVerticle
import cn.bobasyu.base.success
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await

class UserVerticle(
    private val router: Router
) : BaseServiceVerticle() {
    override suspend fun start() {
        setUserRouter()
    }

    private suspend fun setUserRouter() {
        val eventBus: EventBus = vertx.eventBus()

        router.get("/user/query/id/:id").coroutineHandler { ctx ->
            val userId: Int = ctx.pathParam("id").toInt()
            val resp: Message<User> = eventBus.request<User>(USER_QUERY_BY_ID_EVENT, userId).await()
            ctx.response().end(success(resp.body()).toJson())
        }
    }
}

fun Vertx.deployUserVerticle(mySqlClient: MySqlClient, router: Router): Vertx {
    deployVerticle(UserVerticle(router))
    deployVerticle(UserRepositoryVerticle(mySqlClient))
    return this
}