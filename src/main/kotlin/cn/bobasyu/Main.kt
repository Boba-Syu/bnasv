package cn.bobasyu

import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.user.deployUserVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle

class MainVerticle : CoroutineVerticle() {
    private val server: HttpServer by lazy { vertx.createHttpServer() }

    override suspend fun start() {
        val mySqlClient = MySqlClient(vertx)
        val router = Router.router(vertx)
        vertx.deployUserVerticle(mySqlClient, router)

        server.requestHandler(router)
            .listen(8080)
            .onSuccess { println("server start succeed.") }
    }

    override suspend fun stop() {
        server.close()
        vertx.close()
    }
}

fun main() {
    Vertx.vertx().deployVerticle(MainVerticle())
}
