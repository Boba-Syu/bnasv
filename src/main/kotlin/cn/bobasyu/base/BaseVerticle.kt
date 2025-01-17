package cn.bobasyu.base

import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseServiceVerticle(
    applicationContext: ApplicationContext
) : BaseCoroutineVerticle(applicationContext) {
    override suspend fun start() {
        setUserRouter()
    }

    /**
     * HTTP请求handler封装
     */
    inline fun <reified REQ, RESP> Route.doHandler(crossinline fn: suspend (REQ) -> RESP) {
        coroutineHandler { ctx ->
            val requestBodyHandler = requestBodyHandler<REQ>()
            ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
                val req = requestBodyHandler(body)
                when (val resp: RESP = fn(req as REQ)) {
                    is Unit -> ctx.end(success().toJson())
                    else -> ctx.end(success(resp).toJson())
                }
            }
        }
    }

    /**
     * 带有请求体的http请求处理
     */
    inline fun <reified REQ> requestBodyHandler(): (Buffer) -> Any? {
        val requestBodyHandler = when (REQ::class.java) {
            Unit::class.java -> { _: Buffer? -> Unit }
            else -> { body: Buffer -> body.toString().parseJson(REQ::class.java) }
        }
        return requestBodyHandler
    }

    /**
     * 以协程方式注册路由方法
     */
    fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx: RoutingContext ->
            launch(vertx.dispatcher()) {
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
    fun HttpServerRequest.asyncRequestBodyHandler(ctx: RoutingContext, fn: suspend (Buffer) -> Unit) {
        bodyHandler { buffer: Buffer ->
            launch(vertx.dispatcher()) {
                try {
                    fn(buffer)
                } catch (e: Exception) {
                    logger.error(e.message, e)
                    ctx.response().end(failure(e).toJson())
                }
            }
        }
    }

    /**
     * 设置路由
     */
    abstract fun setUserRouter()
}

abstract class BaseRepositoryVerticle(
    applicationContext: ApplicationContext
) : BaseCoroutineVerticle(applicationContext) {
    override suspend fun start() {
        registerConsumer()
    }

    /**
     * 注册总线事件消费方法
     */
    abstract fun registerConsumer()
}

/**
 * 基础verticle封装，添加了很多基于协程的操作方法
 */
open class BaseCoroutineVerticle(
    private val applicationContext: ApplicationContext
) : CoroutineVerticle() {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val SUCCESS = "success"
    }

    /**
     * 以协程方式异步消费总线事件方法
     */
    fun <REQ, RESP> EventBus.asyncConsumer(address: String, handler: (REQ) -> RESP) {
        consumer(address) { message ->
            try {
                val body: REQ = message.body()
                val resp: RESP? = handler(body)
                message.reply(resp)
            } catch (e: Exception) {
                message.fail(500, e.message)
                throw RuntimeException(e)
            }
        }
    }

    /**
     * 以协程方式异步消费总线事件方法
     */
    fun <RESP> EventBus.asyncConsumer(address: String, handler: () -> RESP?) {
        consumer<Unit>(address) { message ->
            try {
                val resp = handler()
                message.reply(resp)
            } catch (e: Exception) {
                message.fail(500, e.message)
                throw RuntimeException(e)
            }
        }
    }

    /**
     * 鉴权方法
     */
    fun Route.authHandler() {
        this.handler(JWTAuthHandler.create(applicationContext.provider))
        this.handler { ctx: RoutingContext ->
            // todo 验证密码
            ctx.next()
        }
    }
}