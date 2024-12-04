package cn.bobasyu.http

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory

/**
 * HTTP客户端封装
 */
class HttpClient(
    val vertx: Vertx,
) {
    companion object {
        private val log = LoggerFactory.getLogger(HttpClient::class.java)
    }

    private val client by lazy {
        OkHttpClient.Builder().build()
    }

    fun close() {}

    /**
     * POST请求
     */
    fun post(url: String, params: Map<String, Any>, headers: Map<String, String>): String? {
        val paramJson = JsonObject()
        params.forEach {
            paramJson.put(it.key, it.value)
        }
        return post(url, paramJson, headers)
    }

    /**
     * POST请求
     */
    fun post(url: String, params: JsonObject, headers: Map<String, String>): String? {
        log.info("HTTP POST: url={}, params={}", url, params)
        val request: Request = with(Request.Builder().url(url)) {
            headers.entries.forEach { (k, v) -> addHeader(k, v) }
            post(params.toString().toRequestBody())
            build()
        }
        val call: Call = client.newCall(request)
        var resp: String? = null
        call.execute().use { response ->
            response.body.use { body ->
                resp = body?.string()
            }
        }
        return resp
    }
}