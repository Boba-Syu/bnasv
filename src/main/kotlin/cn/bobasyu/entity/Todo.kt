package cn.bobasyu.entity

import cn.bobasyu.entity.TodoRecordConstant.CREATE_TIME_COLUMN
import cn.bobasyu.entity.TodoRecordConstant.OTHER_PROPERTIES_COLUMN
import cn.bobasyu.entity.TodoRecordConstant.TITLE_COLUMN
import cn.bobasyu.entity.TodoRecordConstant.TODO_ID_COLUMN
import cn.bobasyu.entity.TodoRecordConstant.TODO_LIST_COLUMN
import cn.bobasyu.entity.TodoRecordConstant.TODO_RECORD
import cn.bobasyu.entity.TodoRecordConstant.UPDATE_TIME_COLUMN
import cn.bobasyu.utils.BaseCodec
import io.vertx.core.eventbus.EventBus
import org.ktorm.entity.Entity
import org.ktorm.jackson.json
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.time.LocalDateTime

object TodoRecordConstant {
    const val TODO_RECORD = "todo_record"
    const val TODO_ID_COLUMN = "todo_id"
    const val TITLE_COLUMN = "title"
    const val TODO_LIST_COLUMN = "todo_list"
    const val OTHER_PROPERTIES_COLUMN = "other_properties"
    const val CREATE_TIME_COLUMN = "create_time"
    const val UPDATE_TIME_COLUMN = "update_time"
}

object TodoRecords : Table<TodoRecord>(TODO_RECORD) {
    val todoId: Column<Long> = long(TODO_ID_COLUMN).primaryKey().bindTo { it.todoId }
    val title: Column<String> = varchar(TITLE_COLUMN).bindTo { it.title }
    val todoList: Column<List<TodoItem>> = json<List<TodoItem>>(TODO_LIST_COLUMN).bindTo { it.todoList }
    val otherProperties: Column<Map<String, Any>> =
        json<Map<String, Any>>(OTHER_PROPERTIES_COLUMN).bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime(CREATE_TIME_COLUMN).bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime(UPDATE_TIME_COLUMN).bindTo { it.updateTime }
}

interface TodoRecord : Entity<TodoRecord> {
    companion object : Entity.Factory<TodoRecord>()

    var todoId: Long
    var title: String
    var todoList: List<TodoItem>
    var otherProperties: Map<String, Any>
    var createTime: LocalDateTime
    var updateTime: LocalDateTime
}

data class TodoItem (
    val itemName : String,
    val itemContent: String,
    val subItems: List<TodoItem>? = null,
    val otherProperties: Map<String, Any> = emptyMap()
)

fun EventBus.registerTodoCodecs(): EventBus = this.apply {
    registerDefaultCodec(TodoRecord::class.java, BaseCodec(TodoRecord::class.java))
    registerDefaultCodec(TodoItem::class.java, BaseCodec(TodoItem::class.java))
}