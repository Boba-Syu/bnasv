package cn.bobasyu.test.bangumi;

import cn.bobasyu.bangumi.BangumiRepository
import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.entity.BangumiSearchDto
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
        val resp = bangumiRepository.searchByKeyword(BangumiSearchDto(keyword = "86"))
        println(resp)
        testContext.completeNow()
    }

    @Test
    fun calender(vertx: Vertx, testContext: VertxTestContext) {
        val bangumiRepository = BangumiRepository(applicationText(vertx))
        val resp = bangumiRepository.calendar()
        println(resp)
        testContext.completeNow()
    }
}
