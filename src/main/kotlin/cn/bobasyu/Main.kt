package cn.bobasyu

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.failure
import cn.bobasyu.base.notFound
import cn.bobasyu.user.deployUserVerticle
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias DeployServiceVerticleHandler = Vertx.(ApplicationContext, Router) -> Vertx

/**
 * MainVerticle 注册全部服务
 */
class MainVerticle(
    private val deployServiceVerticleHandlerList: List<DeployServiceVerticleHandler> = Collections.emptyList(),
    private val port: Int = 8080
) : CoroutineVerticle() {
    private val server: HttpServer by lazy { vertx.createHttpServer() }
    private val applicationContext: ApplicationContext by lazy { ApplicationContext(vertx) }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MainVerticle::class.java)
    }

    override suspend fun start() {
        val router = Router.router(vertx)
        router.registerFailureHandler()
        deployServiceVerticleHandlerList.forEach { vertx.it(applicationContext, router) }

        server.requestHandler(router)
            .listen()
            .onSuccess { logger.info("server start succeed, port=${port}.") }
    }

    override suspend fun stop() {
        applicationContext.close()
        server.close()
        vertx.close()
    }

    private fun Router.registerFailureHandler() {
        route().last().failureHandler { ctx ->
            logger.error("failure request, {}", ctx.request().absoluteURI())
            ctx.response().end(notFound(ctx.request().uri()).toJson())
        }
    }
}

fun main() {
    val mainVerticle = MainVerticle(listOf(Vertx::deployUserVerticle))
    Vertx.vertx().deployVerticle(mainVerticle)
}
