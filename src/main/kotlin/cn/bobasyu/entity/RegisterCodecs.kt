package cn.bobasyu.entity

import cn.bobasyu.utils.BaseCodec
import io.vertx.core.eventbus.EventBus


/**
 * 注册总线中实体类数据传输需要用到的编解码器
 */
fun EventBus.registerCodecs(): EventBus = this.apply {
    registerDefaultCodec(UserInsertDTO::class.java, BaseCodec(UserInsertDTO::class.java))
    registerDefaultCodec(UserLoginDTO::class.java, BaseCodec(UserLoginDTO::class.java))
    registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))
}