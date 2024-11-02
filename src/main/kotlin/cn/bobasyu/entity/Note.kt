package cn.bobasyu.entity

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * 数据库操作实体封装
 */
data class NoteRecord(
    @JsonProperty("note_id") val noteId: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("other_properties") val otherProperties: String = "{}",
    @JsonProperty("create_time") val createTime: LocalDateTime = LocalDateTime.now(),
    @JsonProperty("update_time") val updateTime: LocalDateTime = LocalDateTime.now()
)

data class NoteDto(
    val noteId: Long?,
    val title: String,
    val content: String,
    val otherProperties: String = "{}",
    val createTime: LocalDateTime = LocalDateTime.now(),
    val updateTime: LocalDateTime = LocalDateTime.now()
)