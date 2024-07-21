package cn.bobasyu.utils

import cn.bobasyu.utils.ObjectJson.objectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ObjectJson {
    val objectMapper = jacksonObjectMapper()
}

fun Any.toJson(): String = objectMapper.writeValueAsString(this)

inline fun <reified T> String.parseJson(objectType: Class<T>): T = objectMapper.readValue(this, objectType)
