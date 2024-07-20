package cn.bobasyu.user

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle

class UserVerticle : CoroutineVerticle() {
    override fun init(vertx: Vertx, context: Context) {

    }


}

class UserRepositoryVerticle : CoroutineVerticle() {

}

fun Vertx.deployUserVerticle(): Vertx {
    deployVerticle(UserVerticle())
    deployVerticle(UserRepositoryVerticle())
    return this
}