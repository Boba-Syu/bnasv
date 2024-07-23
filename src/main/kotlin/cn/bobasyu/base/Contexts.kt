package cn.bobasyu.base

import cn.bobasyu.auth.JwtAuth
import cn.bobasyu.databeses.MySqlClient
import io.vertx.core.Vertx

class ApplicationContext(vertx: Vertx) {
    val mySqlClient: MySqlClient by lazy { MySqlClient(vertx) }
    val jwtAuth: JwtAuth by lazy { JwtAuth(vertx) }

    fun close() {
        mySqlClient.close()
    }
}