package cn.bobasyu.constant

/**
 * 用户相关的总线事件名称
 */
object UserRepositoryConsumerConstant {
    /**
     * 查询全部用户信息事件名称
     */
    const val USER_QUERY_EVENT: String = "db.user.query"

    /**
     * 根据用户名和密码进行查询事件名称，登录逻辑中使用
     */
    const val USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT = "db.user.query.by.username.and.password"

    /**
     * 根据用户ID查询用户信息事件名称
     */
    const val USER_QUERY_BY_ID_EVENT: String = "db.user.query.by.id"
    const val USER_INSERT_EVENT = "db.user.insert"
}

object UserRecordConstant {
    const val USER_ID = "userId"
    const val USERNAME = "username"
}