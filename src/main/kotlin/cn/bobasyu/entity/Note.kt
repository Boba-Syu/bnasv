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
 * 数据库操作实体封装
 */
object NoteRecords : Table<NoteRecord>("note_record") {
    val noteId: Column<Long> = long("note_id").primaryKey().bindTo { it.noteId }
    val title: Column<String> = varchar("title").bindTo { it.title }
    val content: Column<String> = varchar("content").bindTo { it.content }
    val otherProperties: Column<Map<String, Any>> = json<Map<String, Any>>("other_properties").bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime("create_time").bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime("update_time").bindTo { it.updateTime }
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

data class NoteVo(
    val noteId: Long,
    val title: String,
    val content: String?,
    val otherProperties: Map<String, Any>,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime
) {
    constructor(noteRecord: NoteRecord) : this(
        noteId = noteRecord.noteId,
        title = noteRecord.title,
        content = noteRecord.content,
        otherProperties = noteRecord.otherProperties,
        createTime = noteRecord.createTime,
        updateTime = noteRecord.updateTime
    )
}

fun EventBus.registerNoteCodecs() : EventBus =this.apply {
    registerDefaultCodec(NoteRecord::class.java, BaseCodec(NoteRecord::class.java))
    registerDefaultCodec(NoteDto::class.java, BaseCodec(NoteDto::class.java))
    registerDefaultCodec(NotePageDto::class.java, BaseCodec(NotePageDto::class.java))
}