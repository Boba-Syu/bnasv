package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.base.BaseServiceVerticle
import cn.bobasyu.base.failure
import cn.bobasyu.base.success
import cn.bobasyu.entity.UserInsertDTO
import cn.bobasyu.entity.UserLoginDTO
import cn.bobasyu.entity.UserRecord
import cn.bobasyu.constant.UserRecordConstant.USERNAME
import cn.bobasyu.constant.UserRecordConstant.USER_ID
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
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
) : BaseServiceVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    private val provider: JWTAuth = applicationContext.provider

    /**
     * 注册路由
     */
    override fun setUserRouter() = with(applicationContext.router) {
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


/**
 * 用户操作Repository抽象类，消费相关总线事件返回数据库操作结果，抽离出数据库操作的具体实现，方便日后更换底层实现
 */
abstract class AbstractUserRepository(
    applicationContext: ApplicationContext
) : BaseRepositoryVerticle(applicationContext) {
    private val eventBus: EventBus by lazy { vertx.eventBus() }

    /**
     * 注册总线事件消费方法
     */
    override fun registerConsumer() = with(eventBus) {
        asyncConsumer(USER_QUERY_EVENT) { handleQueryUserListEvent(it) }
        asyncConsumer(USER_QUERY_BY_ID_EVENT) { handleQueryUserByIdEvent(it) }
        asyncConsumer(USER_INSERT_EVENT) { handleInsertUserEvent(it) }
        asyncConsumer(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT) { handleQueryUserByUsernameAndPasswordEvent(it) }
    }

    abstract suspend fun handleQueryUserListEvent(message: Message<Unit>)
    abstract suspend fun handleQueryUserByIdEvent(message: Message<Int>)
    abstract suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>)
    abstract suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>)
}

/**
 * 用户相关的服务注册
 */
fun Vertx.deployUserVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(UserVerticle(applicationContext))
    deployVerticle(UserRepositoryVerticle(applicationContext))
}
