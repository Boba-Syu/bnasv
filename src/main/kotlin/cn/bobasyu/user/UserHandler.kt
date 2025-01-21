package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseServiceVerticle
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT
import cn.bobasyu.user.UserParamConstant.USERNAME
import cn.bobasyu.user.UserParamConstant.USER_ID
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.coAwait

/**
 * 用户信息相关操作，包括登录、注册、查询等
 */
class UserVerticle(
    val applicationContext: ApplicationContext,
) : BaseServiceVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    private val provider: JWTAuth = applicationContext.provider

    /**
     * 注册路由
     */
    override fun setUserRouter() = with(applicationContext.router) {
        post("/login").doHandler(::loginHandler)
        post("/register").doHandler(::queryRegisterHandler)

        route("/user/*").authHandler()
        get("/user").doHandler<Long, UserRecord>(::queryByIdHandler)
    }

    private suspend fun loginHandler(userLoginDTO: UserLoginDTO): String {
        val userRecord: UserRecord =
            eventBus.request<UserRecord>(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT, userLoginDTO)
                .coAwait().body()

        // 使用jwt做鉴权
        return provider.generateToken(json {
            obj(
                USER_ID to userRecord.userId,
                USERNAME to userRecord.username
            )
        })
    }

    private suspend fun queryByIdHandler(userId: Long): UserRecord {
        val message: Message<UserRecord> = eventBus.request<UserRecord>(USER_QUERY_BY_ID_EVENT, userId).coAwait()
        return message.body()
    }

    private suspend fun queryRegisterHandler(userInsertDTO: UserInsertDTO): String {
        val message = eventBus.request<String>(USER_INSERT_EVENT, userInsertDTO).coAwait()
        return message.body()
    }
}


/**
 * 用户相关的服务注册
 */
fun Vertx.deployUserVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(UserVerticle(applicationContext))
    deployVerticle(UserRepositoryVerticle(applicationContext))
}
