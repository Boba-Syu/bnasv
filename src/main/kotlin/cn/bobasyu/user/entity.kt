package cn.bobasyu.user

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 数据库查询结果封装实体类
 */
data class UserRecord(
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("username") val username: String,
    @JsonProperty("password") val password: String,
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
    val userName: Int,
    val password: String,
)