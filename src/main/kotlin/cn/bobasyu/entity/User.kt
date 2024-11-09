package cn.bobasyu.entity

import cn.bobasyu.databeses.DatabaseHandler
import cn.bobasyu.utils.BaseCodec
import io.vertx.core.eventbus.EventBus
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.json
import org.ktorm.schema.*
import java.time.LocalDateTime

/**
 * 数据库查询结果封装实体类
 */
object UserRecords : Table<UserRecord>("user_record") {
    val userId: Column<Long> = long("user_id").primaryKey().bindTo { it.userId }
    val username: Column<String> = varchar("username").bindTo { it.username }
    val password: Column<String> = varchar("password").bindTo { it.password }
    val otherProperties: Column<Map<String, Any>> =  json<Map<String, Any>>("other_properties").bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime("create_time").bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime("update_time").bindTo { it.updateTime }
}

interface UserRecord : Entity<UserRecord> {
    companion object : Entity.Factory<UserRecord>()

    var userId: Long
    var username: String
    var password: String
    var otherProperties: Map<String, Any>
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}

val DatabaseHandler.userRecords get() = this.database.sequenceOf(UserRecords)

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