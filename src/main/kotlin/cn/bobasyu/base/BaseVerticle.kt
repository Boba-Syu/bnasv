package cn.bobasyu.base

import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch

open class BaseServiceVerticle : CoroutineVerticle() {

    fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            launch(ctx.vertx().dispatcher()) { fn(ctx) }
        }
    }
}

open class BaseRepositoryVerticle : CoroutineVerticle() {

    suspend fun <T> EventBus.asyncConsumer(address: String, handler: suspend (Message<T>) -> Unit) {
        consumer(address) {
            launch(vertx.dispatcher()) { handler(it) }
        }
    }
}