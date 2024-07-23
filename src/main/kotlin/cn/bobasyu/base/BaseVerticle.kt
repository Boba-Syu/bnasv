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

/**
 * 基础verticle封装，添加了很多基于协程的操作方法
 */
open class BaseCoroutineVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 以协程方式注册路由方法
     */
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

    /**
     * 以协程方式对http请求体进行处理方法
     */
    fun HttpServerRequest.asyncRequestBodyHandler(fn: suspend (Buffer) -> Unit) {
        bodyHandler { buffer: Buffer ->
            launch { fn(buffer) }
        }
    }

    /**
     * 以协程方式异步消费总线事件方法
     */
    suspend fun <T> EventBus.asyncConsumer(address: String, handler: suspend (Message<T>) -> Unit) {
        consumer(address) {
            launch(vertx.dispatcher()) { handler(it) }
        }
    }
}
