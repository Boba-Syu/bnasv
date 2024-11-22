package cn.bobasyu.constant

object TodoRepositoryConsumerConstant {
    const val TODO_QUERY_BY_ID_EVENT: String = "db.todo.query.by.id"
    const val TODO_UPDATE_EVENT: String = "db.todo.update"
    const val TODO_PAGE_INFO: String = "db.todo.pageInfo"
}

object TodoParamConstant {
    const val TODO_ID = "todoId"
    const val TITLE = "title"
    const val CONTENT = "content"
    const val TODO_LIST = "todoList"
    const val OTHER_PROPERTIES = "otherProperties"
    const val CREATE_TIME =  "createTime"
    const val UPDATE_TIME =  "updateTime"
}