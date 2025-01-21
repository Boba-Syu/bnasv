package cn.bobasyu.bangumi

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseServiceVerticle
import cn.bobasyu.bangumi.entity.BangumiCalendar
import cn.bobasyu.bangumi.entity.BangumiCalendarWeekdayEnum
import cn.bobasyu.bangumi.entity.BangumiSearchDto
import cn.bobasyu.bangumi.entity.BangumiSubject
import cn.bobasyu.constant.BangumiConsumerConstant.CALENDAR
import cn.bobasyu.constant.BangumiConsumerConstant.FIND_BY_ID
import cn.bobasyu.constant.BangumiConsumerConstant.FIND_BY_KEYWORD
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.coroutines.coAwait

class BangumiHandler(
    private val applicationContext: ApplicationContext
) : BaseServiceVerticle(applicationContext) {
    private val eventBus: EventBus get() = vertx.eventBus()
    private val databaseHandler = applicationContext.databaseHandler

    private val baseUrl = "/bangumi"

    override fun setUserRouter() = with(applicationContext.router) {
        get("${baseUrl}/calendar").doHandler<Unit, Map<BangumiCalendarWeekdayEnum, BangumiCalendar>?> { calendar() }
        post("${baseUrl}/search").doHandler(::search)
        get("${baseUrl}/detail").doHandler(::searchById)

    }

    private suspend fun calendar(): Map<BangumiCalendarWeekdayEnum, BangumiCalendar>? {
        val resp = eventBus.request<Map<BangumiCalendarWeekdayEnum, BangumiCalendar>>(CALENDAR, null).coAwait()
        return resp.body()
    }

    private suspend fun search(bangumiSearchDto: BangumiSearchDto): List<BangumiSubject> {
        val resp = eventBus.request<List<BangumiSubject>>(FIND_BY_KEYWORD, bangumiSearchDto).coAwait()
        return resp.body()
    }

    private suspend fun searchById(bangumiSearchDto: BangumiSearchDto): BangumiSubject {
        val resp = eventBus.request<BangumiSubject>(FIND_BY_ID, bangumiSearchDto).coAwait()
        return resp.body()
    }
}

fun Vertx.deployBangumiVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(BangumiHandler(applicationContext))
    deployVerticle(BangumiRepository(applicationContext))
}