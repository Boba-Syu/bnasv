package cn.bobasyu.base

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch

open class BaseCoroutineVerticle : CoroutineVerticle() {

    fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx: RoutingContext ->
            launch(ctx.vertx().dispatcher()) { fn(ctx) }
        }
    }

    fun HttpServerRequest.asyncRequestBodyHandler(fn: suspend (Buffer) -> Unit) {
        bodyHandler { buffer: Buffer ->
            launch { fn(buffer) }
        }
    }

    suspend fun <T> EventBus.asyncConsumer(address: String, handler: suspend (Message<T>) -> Unit) {
        consumer(address) {
            launch(vertx.dispatcher()) { handler(it) }
        }
    }
}
