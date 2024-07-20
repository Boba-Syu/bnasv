package cn.bobasyu

import cn.bobasyu.user.deployUserVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle

class MainVerticle : CoroutineVerticle() {
    private val server: HttpServer by lazy { vertx.createHttpServer() }

    override suspend fun start() {
        val router = initRouter()
        server.requestHandler(router)
            .listen(8080)
            .onSuccess { println("server start succeed.") }
    }

    override suspend fun stop() {
        server.close()
        vertx.close()
    }

    private fun initRouter(): Router {
        val router = Router.router(vertx)
        router.get("/user/query/id/:id").handler { ctx ->
            val id: Int = ctx.pathParam("id").toInt()
            ctx.response().end("ID: $id")
        }
        return router
    }
}

fun Vertx.deployMainVerticle(): Vertx {
    deployVerticle(MainVerticle())
    return this
}

fun main() {
    Vertx.vertx()
        .deployMainVerticle()
        .deployUserVerticle()
}
