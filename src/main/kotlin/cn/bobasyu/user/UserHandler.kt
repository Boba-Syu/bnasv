package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.base.failure
import cn.bobasyu.base.success
import cn.bobasyu.entity.UserInsertDTO
import cn.bobasyu.entity.UserLoginDTO
import cn.bobasyu.entity.UserRecord
import cn.bobasyu.user.UserRecordConstant.USERNAME
import cn.bobasyu.user.UserRecordConstant.USER_ID
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await

/**
 * 用户信息相关操作，包括登录、注册、查询等
 */
class UserVerticle(
    val applicationContext: ApplicationContext,
) : BaseCoroutineVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    private val provider: JWTAuth = applicationContext.provider

    override suspend fun start() {
        setUserRouter()
    }

    /**
     * 注册路由
     */
    private fun setUserRouter() = with(applicationContext.router) {
        post("/login").coroutineHandler { loginHandler(it) }
        post("/register").coroutineHandler { queryRegisterHandler(it) }

        route("/user/*").authHandler()
        get("/user").coroutineHandler { queryByIdHandler(it) }
    }

    private fun loginHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            // 验证用户名和密码
            val userLoginDTO: UserLoginDTO = body.toString().parseJson(UserLoginDTO::class.java)
            val userRecord: UserRecord =
                eventBus.request<UserRecord>(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT, userLoginDTO)
                    .await().body()

            // 使用jwt做鉴权
            val generateToken: String = provider.generateToken(json {
                obj {
                    USER_ID to userRecord.userId
                    USERNAME to userRecord.username
                }
            })
            ctx.response().end(generateToken)
        }
    }

    private suspend fun queryByIdHandler(ctx: RoutingContext) {
        val userId: Int = ctx.request().getParam(USER_ID).toInt()
        val resp: Message<UserRecord> = eventBus.request<UserRecord>(USER_QUERY_BY_ID_EVENT, userId).await()
        ctx.response().end(success(resp.body()).toJson())
    }

    private fun queryRegisterHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            val json = body.toString()
            val userInsertDTO: UserInsertDTO = json.parseJson(UserInsertDTO::class.java)
            eventBus.request<String>(USER_INSERT_EVENT, userInsertDTO)
                .onSuccess { ctx.response().end(success().toJson()) }
                .onFailure { ctx.response().end(failure(it.message).toJson()) }
        }
    }
}
