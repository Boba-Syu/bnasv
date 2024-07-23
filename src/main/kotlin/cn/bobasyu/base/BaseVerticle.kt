package cn.bobasyu.base

import cn.bobasyu.utils.toJson
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

open class BaseCoroutineVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx: RoutingContext ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    logger.error(e.message, e)
                    ctx.response().end(failure(e).toJson())
                }
            }
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
