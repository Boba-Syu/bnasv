package cn.bobasyu

import cn.bobasyu.bangumi.deployBangumiVerticle
import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.ConfigName
import cn.bobasyu.base.failure
import cn.bobasyu.base.unauthorized
import cn.bobasyu.entity.registerCodecs
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias DeployServiceVerticleHandler = Vertx.(ApplicationContext) -> Vertx

@ConfigName("server")
data class ServerConfig(
    val port: Int
)

/**
 * MainVerticle 注册全部服务
 */
class MainVerticle(
    private val deployServiceVerticleHandlerList: List<DeployServiceVerticleHandler> = Collections.emptyList(),
    private val applicationContext: ApplicationContext,
) : CoroutineVerticle() {
    private val server: HttpServer by lazy { vertx.createHttpServer() }

    private val serverConfig: ServerConfig = applicationContext.config[ServerConfig::class]
    private val port get() = serverConfig.port

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MainVerticle::class.java)
    }

    override suspend fun start() {
        // 路由
        val router = applicationContext.router
        router.registerFailureHandler()

        // 注册handler
        deployServiceVerticleHandlerList.forEach { vertx.it(applicationContext) }

        // 启动服务
        server.requestHandler(router)
            .listen(port)
            .onSuccess { logger.info("server start succeed, port=${port}.") }
    }

    override suspend fun stop() {
        applicationContext.close()
        server.close()
        vertx.close()
    }

    private fun Router.registerFailureHandler() {
        route().last().failureHandler { ctx ->
            if (ctx.failure().message == "Unauthorized") {
                ctx.response().end(unauthorized().toJson())
            } else {

                logger.error("failure request, {}", ctx.request().absoluteURI())
                ctx.response().end(failure(ctx.request().uri()).toJson())
            }
        }
    }
}


fun main() {
    val deployServiceVerticleHandlerList = listOf(
//        Vertx::deployUserVerticle,
//        Vertx::deployNoteVerticle,
        Vertx::deployBangumiVerticle
    )

    val vertx = Vertx.vertx()
    val applicationContext = ApplicationContext(vertx)
    val mainVerticle = MainVerticle(
        deployServiceVerticleHandlerList = deployServiceVerticleHandlerList,
        applicationContext = applicationContext
    )

    vertx.apply {
        eventBus().registerCodecs()
        deployVerticle(mainVerticle)
    }
}
