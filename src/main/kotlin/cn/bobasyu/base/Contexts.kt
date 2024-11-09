package cn.bobasyu.base

import cn.bobasyu.auth.JwtAuth
import cn.bobasyu.databeses.SqlClient
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore

/**
 * 上下文封装，各个业务verticle可能需要通道的对象
 */
class ApplicationContext(vertx: Vertx) {
    /**
     * 路由
     */
    val router: Router by lazy {
        Router.router(vertx).apply {
            // 设置session
            val store: LocalSessionStore = LocalSessionStore.create(vertx)
            val sessionHandler: SessionHandler = SessionHandler.create(store)
                .setSessionTimeout(24 * 60 * 60 * 100)
            route().handler(sessionHandler)
        }
    }

    /**
     * 数据库链接
     */
    val sqlClient: SqlClient by lazy { SqlClient() }

    /**
     * Jwt鉴权
     */
    val jwtAuth: JwtAuth by lazy { JwtAuth(vertx) }

    val provider: JWTAuth by lazy { jwtAuth.provider }

    /**
     * 关闭上下文
     */
    fun close() {
        sqlClient.close()
    }
}