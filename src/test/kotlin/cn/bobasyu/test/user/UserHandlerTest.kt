package cn.bobasyu.test.user

import cn.bobasyu.DeployServiceVerticleHandler
import cn.bobasyu.MainVerticle
import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.HttpResult
import cn.bobasyu.user.AbstractUserRepository
import cn.bobasyu.user.UserRecord
import cn.bobasyu.user.UserVerticle
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.auth.User
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import java.time.LocalDateTime
import kotlin.test.assertEquals


@ExtendWith(VertxExtension::class)
class UserHandlerTest {

    private val userRepositoryVerticle: AbstractUserRepository = Mockito.mock(AbstractUserRepository::class.java)

    private lateinit var userVerticle: UserVerticle

    private fun deployUserTestVerticle(vertx: Vertx): DeployServiceVerticleHandler {
        return { applicationContext: ApplicationContext, router: Router ->
            vertx.apply {
                userVerticle = UserVerticle(applicationContext, router, userRepositoryVerticle)
                vertx.deployVerticle(userVerticle)
            }
        }
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx) {
        val mainVerticle = MainVerticle(listOf(deployUserTestVerticle(vertx)))
        Vertx.vertx().deployVerticle(mainVerticle)
    }


    @Test
    fun queryByIdTest(vertx: Vertx, testContext: VertxTestContext) {
        runBlocking {
            val userRecord = UserRecord(1, "test1", "testPassword1", LocalDateTime.now(), LocalDateTime.now())
            Mockito.`when`(userRepositoryVerticle.queryUserById(1)).thenReturn(Future.succeededFuture(userRecord))

            val resp = userRepositoryVerticle.queryUserById(1).await()
            assertEquals(resp, userRecord)
        }
        testContext.completeNow()
    }

    @Test
    fun queryByHandlerTest(vertx: Vertx, testContext: VertxTestContext) {
        runBlocking {
            val userRecord = UserRecord(1, "test1", "testPassword1", LocalDateTime.now(), LocalDateTime.now())
            Mockito.`when`(userRepositoryVerticle.queryUserById(1)).thenReturn(Future.succeededFuture(userRecord))

            val client = WebClient.create(Vertx.vertx())
            val httpResponse = client.get("/user/query/id/1")
//                .addQueryParam("id", "1")
                .send()
                .await()

            val resp: HttpResult<*> = httpResponse.body().toString().parseJson(HttpResult::class.java)
            val data = resp.data!!.toJson().parseJson(UserRecord::class.java)
            assertEquals(data, userRecord)
            Mockito.verify(userRepositoryVerticle, Mockito.times(1)).queryUserById(1)
        }
        testContext.completeNow()
    }
}