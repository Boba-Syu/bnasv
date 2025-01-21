package cn.bobasyu.user

import cn.bobasyu.constant.BaseRecordConstant.CREATE_TIME_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.OTHER_PROPERTIES_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.UPDATE_TIME_COLUMN
import cn.bobasyu.constant.UserRecordConstant.PASSWORD_COLUMN
import cn.bobasyu.constant.UserRecordConstant.USERNAME_COLUMN
import cn.bobasyu.constant.UserRecordConstant.USER_ID_COLUMN
import cn.bobasyu.constant.UserRecordConstant.USER_RECORD
import cn.bobasyu.databeses.DatabaseHandler
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.json
import org.ktorm.schema.*
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 数据库查询结果封装实体类
 */
object UserRecords : Table<UserRecord>(USER_RECORD) {
    val userId: Column<Long> = long(USER_ID_COLUMN).primaryKey().bindTo { it.userId }
    val username: Column<String> = varchar(USERNAME_COLUMN).bindTo { it.username }
    val password: Column<String> = varchar(PASSWORD_COLUMN).bindTo { it.password }
    val otherProperties: Column<Map<String, Any>> =  json<Map<String, Any>>(OTHER_PROPERTIES_COLUMN).bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime(CREATE_TIME_COLUMN).bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime(UPDATE_TIME_COLUMN).bindTo { it.updateTime }
}

interface UserRecord : Entity<UserRecord>, Serializable {
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
) : Serializable

/**
 * 用户登录参数封装
 */
data class UserLoginDTO(
    val username: String,
    val password: String,
) : Serializable
