package cn.bobasyu.entity

import cn.bobasyu.databeses.DatabaseHandler
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDateTime

/**
 * 数据库操作实体封装
 */
object NoteRecords : Table<NoteRecord>("note_record") {
    val noteId: Column<Long> = long("note_id").primaryKey().bindTo { it.noteId }
    val title: Column<String> = varchar("title").bindTo { it.title }
    val content: Column<String> = varchar("content").bindTo { it.content }
    val otherProperties: Column<String> = varchar("other_properties").bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime("create_time").bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime("update_time").bindTo { it.createTime }
}

interface NoteRecord : Entity<NoteRecord> {
    companion object : Entity.Factory<NoteRecord>()

    var noteId: Long
    var title: String
    var content: String
    var otherProperties: String
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}

val DatabaseHandler.noteRecords get() = this.database.sequenceOf(NoteRecords)

data class NoteDto(
    val noteId: Long?,
    val title: String?,
    val content: String?,
    val otherProperties: String?,
    val createTime: LocalDateTime?,
    val updateTime: LocalDateTime?
)