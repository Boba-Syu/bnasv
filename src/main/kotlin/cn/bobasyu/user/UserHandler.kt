package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseCoroutineVerticle
import cn.bobasyu.base.success
import cn.bobasyu.utils.BaseCodec
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await

/**
 * 用户信息相关操作，包括登录、注册、查询等
 */
class UserVerticle(
    applicationContext: ApplicationContext,
    private val router: Router,
    private val userRepository: AbstractUserRepository
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
        post("/register").coroutineHandler { queryRegisterHandler(it) }

//        route("/user/*").handler(basicAutHandler)
        get("/user").coroutineHandler { queryByIdHandler(it) }
    }

    private suspend fun loginHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            // 验证用户名和密码
            val userLoginDTO: UserLoginDTO = body.toString().parseJson(UserLoginDTO::class.java)
            val userRecord: UserRecord = userRepository.queryUserByUsernameAndPassword(userLoginDTO).await()

            // 使用jwt做鉴权
            ctx.response().end(provider.generateToken(json {
                obj {
                    "userId" to userRecord.userId
                    "username" to userRecord.username
                }
            }))
        }
    }

    private suspend fun queryByIdHandler(ctx: RoutingContext) {
        val userId: Int = ctx.request().getParam("id").toInt()
        val userRecord = userRepository.queryUserById(userId).await()
        ctx.response().end(success(userRecord).toJson())
    }

    private suspend fun queryRegisterHandler(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            val json = body.toString()
            val userInsertDTO: UserInsertDTO = json.parseJson(UserInsertDTO::class.java)
            userRepository.insertUser(userInsertDTO)
            ctx.response().end(success().toJson())
        }
    }
}

/**
 * 用户操作Repository抽象类，消费相关总线事件返回数据库操作结果，抽离出数据库操作的具体实现，方便日后更换底层实现
 */
abstract class AbstractUserRepository {
    abstract suspend fun queryUserList(): Future<List<UserRecord>>

    abstract suspend fun queryUserById(id: Int): Future<UserRecord>

    abstract suspend fun queryUserByUsername(username: String): Future<UserRecord>

    abstract suspend fun insertUser(userInsertDTO: UserInsertDTO): Future<Unit>

    abstract suspend fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): Future<UserRecord>
}

/**
 * 注册总线中实体类数据传输需要用到的编解码器
 */
fun EventBus.registerCodecs(): EventBus = this.apply {
    registerDefaultCodec(UserInsertDTO::class.java, BaseCodec(UserInsertDTO::class.java))
    registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))
}

/**
 * 用户相关的服务注册
 */
fun Vertx.deployUserVerticle(applicationContext: ApplicationContext, router: Router): Vertx = this.apply {
    deployVerticle(UserVerticle(applicationContext, router, UserRepositoryVerticle(applicationContext.mySqlClient)))
}
