package cn.bobasyu.user

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * 数据库查询结果封装实体类
 */
data class UserRecord(
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("username") val username: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("create_time") val createTime: LocalDateTime,
    @JsonProperty("update_time") val updateTime: LocalDateTime
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