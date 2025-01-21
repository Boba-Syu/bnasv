package cn.bobasyu.constant

/**
 * 用户相关的总线事件名称
 */
object UserRepositoryConsumerConstant {
    const val USER_QUERY_EVENT = "db.user.query"
    const val USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT = "db.user.queryByUsernameAndPassword"
    const val USER_QUERY_BY_ID_EVENT = "db.user.queryById"
    const val USER_INSERT_EVENT = "db.user.insert"
}

/**
 * 笔记相关的总线事件名称
 */
object NoteRepositoryConsumerConstant {
    const val NOTE_QUERY_BY_ID_EVENT = "db.note.queryById"
    const val NOTE_UPDATE_EVENT = "db.note.update"
    const val NOTE_PAGE_INFO = "db.note.pageInfo"
}

object TodoRepositoryConsumerConstant {
    const val TODO_QUERY_BY_ID_EVENT = "db.todo.query.by.id"
    const val TODO_UPDATE_EVENT = "db.todo.update"
    const val TODO_PAGE_INFO = "db.todo.pageInfo"
}

object BangumiConsumerConstant {
    const val CALENDAR = "bangumi.calendar"
    const val FIND_BY_KEYWORD = "bangumi.findByKeyword"
    const val FIND_BY_ID = "bangumi.findById"
}

object CatalogConsumerConstant {
    const val CATALOG_PAGE_INFO = "db.catalog.pageInfo"
    const val CATALOG_SAVE = "db.catalog.save"
}