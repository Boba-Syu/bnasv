package cn.bobasyu

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.failure
import cn.bobasyu.base.unauthorized
import cn.bobasyu.repository.deployRepositoryVerticle
import cn.bobasyu.user.UserInsertDTO
import cn.bobasyu.user.UserLoginDTO
import cn.bobasyu.user.UserRecord
import cn.bobasyu.user.deployUserVerticle
import cn.bobasyu.utils.BaseCodec
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias DeployServiceVerticleHandler = Vertx.(ApplicationContext) -> Vertx

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

/**
 * 注册总线中实体类数据传输需要用到的编解码器
 */
fun EventBus.registerCodecs(): EventBus = this.apply {
    registerDefaultCodec(UserInsertDTO::class.java, BaseCodec(UserInsertDTO::class.java))
    registerDefaultCodec(UserLoginDTO::class.java, BaseCodec(UserLoginDTO::class.java))
    registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))
}


fun main() {
    val deployServiceVerticleHandlerList = listOf(
        Vertx::deployUserVerticle,
        Vertx::deployRepositoryVerticle
    )
    val mainVerticle = MainVerticle(deployServiceVerticleHandlerList)

    Vertx.vertx().apply {
        eventBus().registerCodecs()
        deployVerticle(mainVerticle)
    }
}
