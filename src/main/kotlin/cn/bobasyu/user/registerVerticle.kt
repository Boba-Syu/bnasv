package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import io.vertx.core.Vertx

/**
 * 用户相关的服务注册
 */
fun Vertx.deployUserVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(UserVerticle(applicationContext))
}
