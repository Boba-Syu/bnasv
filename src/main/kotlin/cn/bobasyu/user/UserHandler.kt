package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.base.success
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import cn.bobasyu.utils.BaseCodec
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await

/**
 * 用户相关的总线事件名称
 */
object UserRepositoryConsumerConstant {
    /**
     * 查询全部用户信息事件名称
     */
    const val USER_QUERY_EVENT: String = "db.user.query"

    /**
     * 根据用户名和密码进行查询事件名称，登录逻辑中使用
     */
    const val USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT = "db.user.query.by.username.and.word"

    /**
     * 根据用户ID查询用户信息事件名称
     */
    const val USER_QUERY_BY_ID_EVENT: String = "db.user.query.by.id"
    const val USER_INSERT_EVENT = "db.user.insert"
}

/**
 * 用户信息相关操作，包括登录、注册、查询等
 */
class UserVerticle(
    applicationContext: ApplicationContext,
    private val router: Router,
) : BaseCoroutineVerticle() {
    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }
    private val provider: JWTAuth = applicationContext.jwtAuth.provider
    private val basicAutHandler = applicationContext.jwtAuth.basicAutHandler

    override suspend fun start() {
        setUserRouter()
    }

    /**
     * 注册路由
     */
    private suspend fun setUserRouter() = with(router) {
        post("/login").coroutineHandler { loginHandler(it) }
        post("/user/register").coroutineHandler { queryRegisterHandler(it) }

        get("/user/query/id/:id").handler(basicAutHandler).coroutineHandler { queryByIdHandler(it) }
    }

    private suspend fun loginHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler { body: Buffer ->
            // 验证用户名和密码
            val userLoginDTO: UserInsertDTO = body.toString().parseJson(UserInsertDTO::class.java)
            val userRecord: UserRecord =
                eventBus.request<UserRecord>(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT, userLoginDTO)
                    .await().body()
            // 使用jwt做鉴权
            provider.generateToken(json {
                obj {
                    "userId" to userRecord.userId
                    "username" to userRecord.username
                }
            })
            ctx.response().end(success().toString())
        }
    }

    private suspend fun queryByIdHandler(ctx: RoutingContext) {
        val userId: Int = ctx.pathParam("id").toInt()
        val resp: Message<UserRecord> = eventBus.request<UserRecord>(USER_QUERY_BY_ID_EVENT, userId).await()
        ctx.response().end(success(resp.body()).toJson())
    }

    private suspend fun queryRegisterHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler { body: Buffer ->
            val json = body.toString()
            val userInsertDTO: UserInsertDTO = json.parseJson(UserInsertDTO::class.java)
            eventBus.request<Unit>(USER_INSERT_EVENT, userInsertDTO).await()
        }
    }
}

/**
 * 用户操作Repository抽象类，消费相关总线事件返回数据库操作结果，抽离出数据库操作的具体实现，方便日后更换底层实现
 */
abstract class AbstractUserRepository : BaseCoroutineVerticle() {
    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }

    override suspend fun start() {
        registerConsumer()
    }

    /**
     * 注册总线事件消费方法
     */
    private suspend fun registerConsumer() = with(eventBus) {
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
 * 注册总线中实体类数据传输需要用到的编解码器
 */
fun EventBus.registerCodecs(): EventBus = this
    .registerDefaultCodec(UserInsertDTO::class.java, BaseCodec(UserInsertDTO::class.java))
    .registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))

/**
 * 用户相关的服务注册
 */
fun Vertx.deployUserVerticle(applicationContext: ApplicationContext, router: Router): Vertx = this.apply {
    deployVerticle(UserVerticle(applicationContext, router))
    deployVerticle(UserRepositoryVerticle(applicationContext.mySqlClient))
}
