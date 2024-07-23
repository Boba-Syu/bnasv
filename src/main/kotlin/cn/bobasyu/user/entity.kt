package cn.bobasyu.user

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRecord(
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("username") val username: String,
    @JsonProperty("password") val password: String,
)

data class UserInsertDTO(
    val username: String,
    val password: String,
)

data class UserLoginDTO(
    val userName: Int,
    val password: String,
)