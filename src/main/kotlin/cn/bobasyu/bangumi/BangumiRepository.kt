package cn.bobasyu.bangumi

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.base.ConfigName
import cn.bobasyu.bangumi.BangumiParamConstant.DATA
import cn.bobasyu.bangumi.BangumiParamConstant.FILTER
import cn.bobasyu.bangumi.BangumiParamConstant.KEYWORD
import cn.bobasyu.bangumi.BangumiParamConstant.TYPE
import cn.bobasyu.bangumi.entity.BangumiCalendar
import cn.bobasyu.bangumi.entity.BangumiCalendarWeekdayEnum
import cn.bobasyu.bangumi.entity.BangumiSearchDto
import cn.bobasyu.bangumi.entity.BangumiSubject
import cn.bobasyu.http.HttpClientConstant.AUTHORIZATION
import cn.bobasyu.http.HttpClientConstant.USER_AGENT
import cn.bobasyu.utils.parseJson
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

    private val httpClient = applicationContext.httpClient

    private val headers: Map<String, String>
        get() = mapOf(
            AUTHORIZATION to bangumiConfig.authorization,
            USER_AGENT to bangumiConfig.userAgent
        )

    override fun registerConsumer() = with(vertx.eventBus()) {
        asyncConsumer(BangumiConsumerConstant.CALENDAR, ::calendar)
        asyncConsumer(BangumiConsumerConstant.FIND_BY_KEYWORD, ::searchByKeyword)
        asyncConsumer(BangumiConsumerConstant.FIND_BY_ID, ::searchById)
    }

    /**
     * 新番时刻表
     */
    fun calendar(): Map<BangumiCalendarWeekdayEnum, BangumiCalendar> {
        val url = "${bangumiConfig.baseUrl}/calendar"
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
                FILTER to obj(
                    TYPE to (bangumiSearchDto.types?.map { it.code }?.toList() ?: listOf())
                ),
                KEYWORD to bangumiSearchDto.keyword
            )
        }
        val resp: String? = httpClient.post(url, params, headers)
        val parseMap: Map<String, Any>? = resp?.parseJsonToMap()
        return parseMap?.get(DATA)?.toJson()?.parseJsonToList(BangumiSubject::class.java) ?: listOf()
    }

    fun searchById(subjectId: Int): BangumiSubject? {
        val url = "${bangumiConfig.baseUrl}/v0/subjects/${subjectId}"
        val resp: String? = httpClient.get(url, null, headers)
        return resp?.parseJson(BangumiSubject::class.java)
    }
}