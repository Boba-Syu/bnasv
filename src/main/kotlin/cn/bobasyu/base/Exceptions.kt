package cn.bobasyu.base

class NoSuchRecordInDatabaseException(
    message: String = "No such record in database.",
    cause: Throwable? = null
) : RuntimeException(message, cause)

