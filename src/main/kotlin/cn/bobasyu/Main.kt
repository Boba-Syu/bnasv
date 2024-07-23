package cn.bobasyu

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.failure
import cn.bobasyu.user.deployUserVerticle
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * MainVerticle 注册全部服务
 */
class MainVerticle : CoroutineVerticle() {
    private val server: HttpServer by lazy { vertx.createHttpServer() }
    private val applicationContext: ApplicationContext by lazy { ApplicationContext(vertx) }

    override suspend fun start() {
        val router = Router.router(vertx)
        router.registerFailureHandler()
        vertx.deployUserVerticle(applicationContext, router)

        server.requestHandler(router)
            .listen(8080)
            .onSuccess { println("server start succeed.") }
    }

    override suspend fun stop() {
        applicationContext.close()
        server.close()
        vertx.close()
    }

    private fun Router.registerFailureHandler() {
        route().last().failureHandler { ctx ->
            ctx.response().end(failure(ctx.failure().message).toJson())
        }
    }
}

fun main() {
    Vertx.vertx().deployVerticle(MainVerticle())
}
