package cn.bobasyu.bangumi

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.entity.BangumiSearchDto
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj


class BangumiRepository(
    applicationContext: ApplicationContext
) {
    private val authorization = "Bearer znm8C3TiWuCY98rxYTT4mWwEDqoaGir3rtjFrycV"
    private val baseUrl = "https://api.bgm.tv"

    private val httpClient = applicationContext.httpClient


    private val headers: Map<String, String>
        get() = mapOf(
            "Authorization" to authorization,
            "User-Agent" to "bobasyu/my-private-project"
        )

    fun calendar(): String {
        val url = "$baseUrl/calendar"

        return httpClient.get(url, null, headers)!!
    }

    fun searchByKeyword(bangumiSearchDto: BangumiSearchDto): String {
        val url = "$baseUrl/v0/search/subjects"
        val params: JsonObject = json {
            if (bangumiSearchDto.types != null && bangumiSearchDto.types.isNotEmpty()) {
                obj("types" to bangumiSearchDto.types)
            }
            obj("keyword" to bangumiSearchDto.keyword)
        }
        return httpClient.post(url, params, headers)!!
    }
}