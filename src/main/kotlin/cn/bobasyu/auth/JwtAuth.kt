package cn.bobasyu.auth

import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf

/**
 * Jwt鉴权需要使用的相关对象封装
 */
class JwtAuth(vertx: Vertx) {
    val provider: JWTAuth by lazy { initJwtProvider(vertx) }
    val basicAutHandler: BasicAuthHandler by lazy { BasicAuthHandler.create(provider) }

    private fun initJwtProvider(vertx: Vertx): JWTAuth {
        val jwtAuthOptions: JWTAuthOptions = jwtAuthOptionsOf()
        return JWTAuth.create(vertx, jwtAuthOptions)
    }
}