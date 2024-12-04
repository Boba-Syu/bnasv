package cn.bobasyu.bangumi

import cn.bobasyu.base.ApplicationContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj


class BangumiRepository(
    applicationContext: ApplicationContext
) {
    private val authorization = "znm8C3TiWuCY98rxYTT4mWwEDqoaGir3rtjFrycV"
    private val baseUrl = "https://api.bgm.tv"

    private val httpClient = applicationContext.httpClient


    fun searchByKeyword(keyword: String): String {
        val url = "$baseUrl/v0/search/subjects"
        val params = json {
            obj { "keyword" to keyword }
        }
        val headers = mapOf(
            "Authorization" to authorization,
            "User-Agent" to "bobasyu/my-private-project"
        )
        return httpClient.post(url, params, headers)
    }
}