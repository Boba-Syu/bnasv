package cn.bobasyu.entity

import cn.bobasyu.utils.BaseCodec
import com.fasterxml.jackson.annotation.JsonProperty
import io.vertx.core.eventbus.EventBus
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.LocalDateTime

/**
 * 数据库查询结果封装实体类
 */
@Entity
open class UserRecord(
    @Id
    @GeneratedValue
    @JsonProperty("user_id")
    var userId: Int? = null,

    @JsonProperty("username")
    var username: String? = null,

    @JsonProperty("password")
    var password: String? = null,

    @JsonProperty("other_properties")
    var otherProperties: String? = "{}",

    @JsonProperty("create_time")
    var createTime: LocalDateTime? = LocalDateTime.now(),

    @JsonProperty("update_time")
    var updateTime: LocalDateTime? = LocalDateTime.now()
)

/**
 * 新增用户请求参数封装
 */
data class UserInsertDTO(
    val username: String,
    val password: String,
)

/**
 * 用户登录参数封装
 */
data class UserLoginDTO(
    val username: String,
    val password: String,
)

fun EventBus.registerUserCodecs(): EventBus = this.apply {
    registerDefaultCodec(UserInsertDTO::class.java, BaseCodec(UserInsertDTO::class.java))
    registerDefaultCodec(UserLoginDTO::class.java, BaseCodec(UserLoginDTO::class.java))
    registerDefaultCodec(UserRecord::class.java, BaseCodec(UserRecord::class.java))
}