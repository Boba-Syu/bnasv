package cn.bobasyu.bangumi

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.base.ConfigName
import cn.bobasyu.constant.BangumiConsumerConstant
import cn.bobasyu.entity.BangumiCalendar
import cn.bobasyu.entity.BangumiCalendarWeekdayEnum
import cn.bobasyu.entity.BangumiSearchDto
import cn.bobasyu.entity.BangumiSubject
import cn.bobasyu.utils.ObjectJson
import cn.bobasyu.utils.parseJsonToList
import cn.bobasyu.utils.parseJsonToMap
import cn.bobasyu.utils.toJson
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.stream.Collectors

@ConfigName("bangumi")
class BangumiConfig(
    val authorization: String,
    val baseUrl: String,
    val userAgent: String
)

class BangumiRepository(
    applicationContext: ApplicationContext
) : BaseRepositoryVerticle(applicationContext) {
    private val bangumiConfig: BangumiConfig = applicationContext.config[BangumiConfig::class]
    private val authorization get() = bangumiConfig.authorization
    private val baseUrl get() = bangumiConfig.baseUrl
    private val userAgent get() = bangumiConfig.userAgent

    private val httpClient = applicationContext.httpClient

    private val headers: Map<String, String>
        get() = mapOf(
            "Authorization" to authorization,
            "User-Agent" to userAgent
        )

    override fun registerConsumer() = with(vertx.eventBus()) {
        asyncConsumer<Map<BangumiCalendarWeekdayEnum, BangumiCalendar>>(BangumiConsumerConstant.CALENDAR) { calendar() }
        asyncConsumer<BangumiSearchDto, List<BangumiSubject>>(BangumiConsumerConstant.FIND_BY_KEYWORD) { searchByKeyword(it) }
    }

    /**
     * 新番时刻表
     */
    fun calendar(): Map<BangumiCalendarWeekdayEnum, BangumiCalendar> {
        val url = "${baseUrl}/calendar"
        val resp: String = httpClient.get(url, null, headers)!!
        val calendarList: List<BangumiCalendar> = resp.parseJsonToList(BangumiCalendar::class.java)
        return calendarList.stream().collect(Collectors.toMap({ it.items[0].airWeekday }, { it }))
    }

    /**
     * 根据关键词查询词条
     */
    fun searchByKeyword(bangumiSearchDto: BangumiSearchDto): List<BangumiSubject> {
        val url = "${bangumiConfig.baseUrl}/v0/search/subjects"
        val params: JsonObject = json {
            obj(
                "filter" to obj (
                   "type" to (bangumiSearchDto.types?.map { it.code }?.toList() ?: listOf())
                ),
                "keyword" to bangumiSearchDto.keyword
            )
        }
        val resp: String? = httpClient.post(url, params, headers)
        val parseMap: Map<String, Any>? = resp?.parseJsonToMap()
        return parseMap?.get("data")?.toJson()?.parseJsonToList(BangumiSubject::class.java) ?: listOf()
    }
}