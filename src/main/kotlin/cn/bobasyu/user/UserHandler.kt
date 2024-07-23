package cn.bobasyu.user

import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.base.success
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.user.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import cn.bobasyu.utils.BaseCodec
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await

class UserVerticle(
    private val router: Router,
) : BaseCoroutineVerticle() {

    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }

    override suspend fun start() {
        setUserRouter()
    }

    private suspend fun setUserRouter() = with(router) {
        get("/user/query/id/:id").coroutineHandler { queryByIdHandler(it) }
        post("/user/register").coroutineHandler { queryRegisterHandler(it) }
    }

    private suspend fun queryByIdHandler(ctx: RoutingContext) {
        val userId: Int = ctx.pathParam("id").toInt()
        val resp: Message<UserRecord> = eventBus.request<UserRecord>(USER_QUERY_BY_ID_EVENT, userId).await()
        ctx.response().end(success(resp.body()).toJson())
    }

    private suspend fun queryRegisterHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler { body: Buffer ->
            val json = body.toString()
            val insertUserDto: InsertUserDto = json.parseJson(InsertUserDto::class.java)
            eventBus.request<Unit>(USER_INSERT_EVENT, insertUserDto).await()
        }
    }
}


abstract class AbstractUserRepository : BaseCoroutineVerticle() {

    private val eventBus: EventBus by lazy { vertx.eventBus().registerCodecs() }

    override suspend fun start() {
        registerConsumer()
    }

    private suspend fun registerConsumer() = with(eventBus) {
        registerQueryUserListEvent()
        registerQueryUserByIdEvent()
        registerInsertUserEvent()
    }

    private suspend fun EventBus.registerQueryUserListEvent() {
        asyncConsumer(USER_QUERY_EVENT) { handleQueryUserListEvent(it) }
    }

    private suspend fun EventBus.registerQueryUserByIdEvent() {
        asyncConsumer(USER_QUERY_BY_ID_EVENT) { handleQueryUserByIdEvent(it) }
    }

    private suspend fun EventBus.registerInsertUserEvent() {
        asyncConsumer(USER_INSERT_EVENT) { handleInsertUserEvent(it) }
    }

    abstract suspend fun handleQueryUserListEvent(message: Message<Unit>)

    abstract suspend fun handleQueryUserByIdEvent(message: Message<Int>)

    abstract fun handleInsertUserEvent(message: Message<InsertUserDto>)
}


fun EventBus.registerCodecs(): EventBus = this
    .registerDefaultCodec(InsertUserDto::class.java, BaseCodec(InsertUserDto::class.java))
    .registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))


fun Vertx.deployUserVerticle(mySqlClient: MySqlClient, router: Router): Vertx = this.apply {
    deployVerticle(UserVerticle(router))
    deployVerticle(UserRepositoryVerticle(mySqlClient))
}
