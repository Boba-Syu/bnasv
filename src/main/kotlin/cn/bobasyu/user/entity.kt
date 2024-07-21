package cn.bobasyu.user

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRecord(
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("user_name") val userName: String,
    @JsonProperty("user_password") val userPassword: String,
)

data class InsertUserDto(
    val userName: String,
    val userPassword: String,
)