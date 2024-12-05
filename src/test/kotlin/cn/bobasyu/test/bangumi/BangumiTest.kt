package cn.bobasyu.test.bangumi;

import cn.bobasyu.bangumi.BangumiRepository
import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.entity.BangumiSearchDto
import cn.bobasyu.entity.BangumiSubject
import cn.bobasyu.entity.BangumiSubjectType
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import kotlin.test.assertEquals

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

    @Test
    fun jsonTest() {
        val bangumiSubject =BangumiSubject(
            id=0,
            type = BangumiSubjectType.ANIMATION,
            date = LocalDate.now(),
        )
        print(bangumiSubject.toJson())
    }

    @Test
    fun jsonTest2() {
        val bangumiSubject =BangumiSubject(
            id=0,
            type = BangumiSubjectType.ANIMATION,
            date = LocalDate.now(),
        )
        val json = """
            {"id":0,"type":2,"name":"","name_cn":"","summary":"","date":"2024-12-05","platform":"","images":{"large":"","common":"","medium":"","small":"","grid":""},"infoBox":"","volumes":null,"total_episodes":null}
        """.trimIndent()
        assertEquals(bangumiSubject, json.parseJson(BangumiSubject::class.java))
    }
}
