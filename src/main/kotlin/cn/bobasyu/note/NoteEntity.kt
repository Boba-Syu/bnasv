package cn.bobasyu.note

import cn.bobasyu.base.PageVal
import cn.bobasyu.constant.BaseRecordConstant.CREATE_TIME_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.OTHER_PROPERTIES_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.UPDATE_TIME_COLUMN
import cn.bobasyu.constant.NoteRecordConstant.CONTENT_COLUMN
import cn.bobasyu.constant.NoteRecordConstant.NOTE_ID_COLUMN
import cn.bobasyu.constant.NoteRecordConstant.NOTE_RECORD
import cn.bobasyu.constant.NoteRecordConstant.TITLE_COLUMN
import cn.bobasyu.databeses.DatabaseHandler
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.json
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.io.Serializable
import java.time.LocalDateTime


/**
 * 数据库操作实体封装
 */
object NoteRecords : Table<NoteRecord>(NOTE_RECORD) {
    val noteId: Column<Long> = long(NOTE_ID_COLUMN).primaryKey().bindTo { it.noteId }
    val title: Column<String> = varchar(TITLE_COLUMN).bindTo { it.title }
    val content: Column<String> = varchar(CONTENT_COLUMN).bindTo { it.content }
    val otherProperties: Column<Map<String, Any>> =
        json<Map<String, Any>>(OTHER_PROPERTIES_COLUMN).bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime(CREATE_TIME_COLUMN).bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime(UPDATE_TIME_COLUMN).bindTo { it.updateTime }
}

interface NoteRecord : Entity<NoteRecord>, Serializable {
    companion object : Entity.Factory<NoteRecord>()

    var noteId: Long
    var title: String
    var content: String
    var otherProperties: Map<String, Any>
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}

val DatabaseHandler.noteRecords get() = this.database.sequenceOf(NoteRecords)

data class NoteDto(
    val noteId: Long?,
    val title: String?,
    val content: String?,
    val otherProperties: Map<String, Any>?,
    val updateTime: LocalDateTime?
) : Serializable

data class NotePageDto(
    val pageVal: PageVal,

    val noteId: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val createTimeBegin: LocalDateTime? = null,
    val createTimeEnd: LocalDateTime? = null,
    val updateTimeBegin: LocalDateTime? = null,
    val updateTimeEnd: LocalDateTime? = null
) : Serializable
