package cn.bobasyu.constant

/**
 * 笔记相关的总线事件名称
 */
object NoteRepositoryConsumerConstant {
    const val NOTE_QUERY_BY_ID_EVENT: String = "db.note.query.by.id"
    const val NOTE_UPDATE_EVENT: String = "db.note.update"
    const val NOTE_PAGE_INFO: String = "db.note.pageInfo"
}

object NoteParamConstant {
    const val NOTE_ID = "noteId"
    const val TITLE = "title"
    const val CONTENT = "content"
    const val OTHER_PROPERTIES = "otherProperties"
    const val CREATE_TIME =  "createTime"
    const val UPDATE_TIME =  "updateTime"
}