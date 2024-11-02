package cn.bobasyu.entity

import io.vertx.core.eventbus.EventBus


/**
 * 注册总线中实体类数据传输需要用到的编解码器
 */
fun EventBus.registerCodecs(): EventBus = this.apply {
    registerUserCodecs()
}