package cn.bobasyu.entity

import cn.bobasyu.databeses.DatabaseHandler
import cn.bobasyu.entity.NoteRecordConstant.CONTENT_COLUMN
import cn.bobasyu.entity.NoteRecordConstant.CREATE_TIME_COLUMN
import cn.bobasyu.entity.NoteRecordConstant.NOTE_ID_COLUMN
import cn.bobasyu.entity.NoteRecordConstant.NOTE_RECORD
import cn.bobasyu.entity.NoteRecordConstant.OTHER_PROPERTIES_COLUMN
import cn.bobasyu.entity.NoteRecordConstant.TITLE_COLUMN
import cn.bobasyu.entity.NoteRecordConstant.UPDATE_TIME_COLUMN
import cn.bobasyu.utils.BaseCodec
import io.vertx.core.eventbus.EventBus
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.json
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.time.LocalDateTime

object NoteRecordConstant {
    const val NOTE_RECORD = "note_record"
    const val NOTE_ID_COLUMN = "note_id"
    const val TITLE_COLUMN = "title"
    const val CONTENT_COLUMN = "content"
    const val OTHER_PROPERTIES_COLUMN = "other_properties"
    const val CREATE_TIME_COLUMN = "create_time"
    const val UPDATE_TIME_COLUMN = "update_time"
}

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

interface NoteRecord : Entity<NoteRecord> {
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
    val createTime: LocalDateTime?,
    val updateTime: LocalDateTime?
)

data class NotePageDto(
    val pageNum: Int,
    val pageSize: Int,

    val noteId: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val createTimeBegin: LocalDateTime? = null,
    val createTimeEnd: LocalDateTime? = null,
    val updateTimeBegin: LocalDateTime? = null,
    val updateTimeEnd: LocalDateTime? = null
)

fun EventBus.registerNoteCodecs() : EventBus =this.apply {
    registerDefaultCodec(NoteRecord::class.java, BaseCodec(NoteRecord::class.java))
    registerDefaultCodec(NoteDto::class.java, BaseCodec(NoteDto::class.java))
    registerDefaultCodec(NotePageDto::class.java, BaseCodec(NotePageDto::class.java))
}