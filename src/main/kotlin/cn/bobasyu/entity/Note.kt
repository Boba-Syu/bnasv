package cn.bobasyu.entity

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.LocalDateTime

/**
 * 数据库操作实体封装
 */
@Entity
open class NoteRecord(
    @Id
    @GeneratedValue
    @JsonProperty("note_id")
    var noteId: Long? = null,

    @JsonProperty("title")
    var title: String? = null,

    @JsonProperty("content")
    var content: String? = null,

    @JsonProperty("other_properties")
    var otherProperties: String? = "{}",

    @JsonProperty("create_time")
    var createTime: LocalDateTime? = LocalDateTime.now(),

    @JsonProperty("update_time")
    var updateTime: LocalDateTime? = LocalDateTime.now()
)

data class NoteDto(
    val noteId: Long?,
    val title: String,
    val content: String,
    val otherProperties: String = "{}",
    val createTime: LocalDateTime = LocalDateTime.now(),
    val updateTime: LocalDateTime = LocalDateTime.now()
)