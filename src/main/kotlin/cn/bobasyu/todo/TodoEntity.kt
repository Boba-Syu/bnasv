package cn.bobasyu.todo

import cn.bobasyu.constant.BaseRecordConstant.CREATE_TIME_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.OTHER_PROPERTIES_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.UPDATE_TIME_COLUMN
import cn.bobasyu.constant.TodoRecordConstant.TITLE_COLUMN
import cn.bobasyu.constant.TodoRecordConstant.TODO_ID_COLUMN
import cn.bobasyu.constant.TodoRecordConstant.TODO_LIST_COLUMN
import cn.bobasyu.constant.TodoRecordConstant.TODO_RECORD
import cn.bobasyu.databeses.DatabaseHandler
import cn.bobasyu.note.noteRecords
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



object TodoRecords : Table<TodoRecord>(TODO_RECORD) {
    val todoId: Column<Long> = long(TODO_ID_COLUMN).primaryKey().bindTo { it.todoId }
    val title: Column<String> = varchar(TITLE_COLUMN).bindTo { it.title }
    val todoList: Column<List<TodoItem>> = json<List<TodoItem>>(TODO_LIST_COLUMN).bindTo { it.todoList }
    val otherProperties: Column<Map<String, Any>> =
        json<Map<String, Any>>(OTHER_PROPERTIES_COLUMN).bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime(CREATE_TIME_COLUMN).bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime(UPDATE_TIME_COLUMN).bindTo { it.updateTime }
}

interface TodoRecord : Entity<TodoRecord>, Serializable {
    companion object : Entity.Factory<TodoRecord>()

    var todoId: Long
    var title: String
    var todoList: List<TodoItem>
    var otherProperties: Map<String, Any>
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}

val DatabaseHandler.todoRecords get() = this.database.sequenceOf(TodoRecords)

data class TodoItem (
    val itemName : String,
    val itemContent: String,
    val subItems: List<TodoItem>? = null,
    val otherProperties: Map<String, Any> = emptyMap()
) : Serializable
