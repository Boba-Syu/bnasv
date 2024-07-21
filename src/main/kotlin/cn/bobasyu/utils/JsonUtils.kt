package cn.bobasyu.utils

import cn.bobasyu.utils.ObjectJson.objectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

object ObjectJson {
    val objectMapper = jacksonObjectMapper()
}

fun Any.toJson(): String = objectMapper.writeValueAsString(this)

fun <T> String.parseJson(objectType: Class<T>): T = objectMapper.readValue(this, objectType)


class BaseCodec<T : Any>(private val type: Class<T>) : MessageCodec<T, T> {

    override fun encodeToWire(buffer: Buffer, t: T) {
        buffer.appendString(t.toJson())
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): T =
        buffer.getString(pos, buffer.length()).parseJson(type)

    override fun transform(t: T): T = t

    override fun name(): String =type.name

    override fun systemCodecID(): Byte = -1
}