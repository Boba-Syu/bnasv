package cn.bobasyu.user

import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.base.success
import cn.bobasyu.databeses.MySqlClient
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
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf

object UserRepositoryConsumerConstant {
    const val USER_QUERY_EVENT: String = "db.user.query"
    const val USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT = "db.user.query.by.username.and.word"
    const val USER_QUERY_BY_ID_EVENT: String = "db.user.query.by.id"
    const val USER_INSERT_EVENT = "db.user.insert"
}

class UserVerticle(
    private val router: Router,
) : BaseCoroutineVerticle() {

    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }

    private val provider: JWTAuth by lazy {
        val jwtAuthOptions: JWTAuthOptions = jwtAuthOptionsOf()
        JWTAuth.create(vertx, jwtAuthOptions)
    }
    private val basicAutHandler by lazy { BasicAuthHandler.create(provider) }

    override suspend fun start() {
        setUserRouter()
    }

    private suspend fun setUserRouter() = with(router) {
        post("/login").coroutineHandler { loginHandler(it) }

        get("/user/query/id/:id").handler(basicAutHandler).coroutineHandler { queryByIdHandler(it) }
        post("/user/register").coroutineHandler { queryRegisterHandler(it) }
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


abstract class AbstractUserRepository : BaseCoroutineVerticle() {

    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }

    override suspend fun start() {
        registerConsumer()
    }

    private suspend fun registerConsumer() = with(eventBus) {
        asyncConsumer(USER_QUERY_EVENT) { handleQueryUserListEvent(it) }
        asyncConsumer(USER_QUERY_BY_ID_EVENT) { handleQueryUserByIdEvent(it) }
        asyncConsumer(USER_INSERT_EVENT) { handleInsertUserEvent(it) }
        asyncConsumer(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT) { handleQueryUserByUsernameAndPasswordEvent(it) }
    }

    abstract suspend fun handleQueryUserListEvent(message: Message<Unit>)
    abstract suspend fun handleQueryUserByIdEvent(message: Message<Int>)
    abstract fun handleInsertUserEvent(message: Message<UserInsertDTO>)
    abstract suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>)
}


fun EventBus.registerCodecs(): EventBus = this
    .registerDefaultCodec(UserInsertDTO::class.java, BaseCodec(UserInsertDTO::class.java))
    .registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))


fun Vertx.deployUserVerticle(mySqlClient: MySqlClient, router: Router): Vertx = this.apply {
    deployVerticle(UserVerticle(router))
    deployVerticle(UserRepositoryVerticle(mySqlClient))
}
