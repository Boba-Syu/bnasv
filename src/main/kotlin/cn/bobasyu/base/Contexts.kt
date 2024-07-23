package cn.bobasyu.base

import cn.bobasyu.auth.JwtAuth
import cn.bobasyu.databeses.MySqlClient
import io.vertx.core.Vertx

/**
 * 上下文封装，各个业务verticle可能需要通道的对象
 */
class ApplicationContext(vertx: Vertx) {
    /**
     * 数据库链接
     */
    val mySqlClient: MySqlClient by lazy { MySqlClient(vertx) }

    /**
     * Jwt鉴权
     */
    val jwtAuth: JwtAuth by lazy { JwtAuth(vertx) }

    /**
     * 关闭上下文
     */
    fun close() {
        mySqlClient.close()
    }
}