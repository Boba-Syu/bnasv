package cn.bobasyu.base

open class BaseException(
    val code: Int = 500,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)


class NoSuchRecordInDatabaseException(
    code: Int = 500,
    message: String = "No such record in database.",
    cause: Throwable? = null
) : BaseException(code, message, cause)

