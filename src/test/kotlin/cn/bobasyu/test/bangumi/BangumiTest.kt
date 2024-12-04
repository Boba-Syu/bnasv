package cn.bobasyu.test.bangumi;

import cn.bobasyu.bangumi.BangumiRepository
import cn.bobasyu.base.ApplicationContext
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension::class)
class BangumiTest {

    fun applicationText(vertx: Vertx): ApplicationContext  = ApplicationContext(vertx)

    @Test
    fun searchTest(vertx: Vertx, testContext: VertxTestContext) {
        val bangumiRepository = BangumiRepository(applicationText(vertx))
        val resp = bangumiRepository.searchByKeyword("86")
        println(resp)
        testContext.completeNow()
    }
}
