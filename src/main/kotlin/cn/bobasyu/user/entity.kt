package cn.bobasyu.user

import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import com.fasterxml.jackson.annotation.JsonProperty
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

data class User(
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("user_name") val userName: String,
    @JsonProperty("user_password") val userPassword: String,
)

class UserCodec : MessageCodec<User, User> {
    override fun encodeToWire(buffer: Buffer, user: User) {
        buffer.appendString(user.toJson())
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): User =
        buffer.getString(pos, buffer.length()).parseJson(User::class.java)

    override fun transform(user: User): User = user

    override fun name(): String = User::class.java.name

    override fun systemCodecID(): Byte = -1
}