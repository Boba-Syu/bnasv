package cn.bobasyu.repository

import cn.bobasyu.base.ApplicationContext
import io.vertx.core.Vertx


/**
 * 用户相关的服务注册
 */
fun Vertx.deployRepositoryVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(UserRepositoryVerticle(applicationContext))
}
