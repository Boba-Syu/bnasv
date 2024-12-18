package cn.bobasyu.bangumi

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.ConfigName
import cn.bobasyu.entity.BangumiSearchDto
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

@ConfigName("bangumi")
class BangumiConfig(
    val authorization: String,
    val baseUrl: String,
    val userAgent: String
)

class BangumiRepository(
    applicationContext: ApplicationContext
) {
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

    /**
     * 新番时刻表
     */
    fun calendar(): String {
        val url = "${baseUrl}/calendar"

        return httpClient.get(url, null, headers)!!
    }

    /**
     * 根据关键词查询词条
     */
    fun searchByKeyword(bangumiSearchDto: BangumiSearchDto): String {
        val url = "${bangumiConfig.baseUrl}/v0/search/subjects"
        val params: JsonObject = json {
            if (bangumiSearchDto.types != null && bangumiSearchDto.types.isNotEmpty()) {
                obj("types" to bangumiSearchDto.types)
            }
            obj("keyword" to bangumiSearchDto.keyword)
        }
        return httpClient.post(url, params, headers)!!
    }
}