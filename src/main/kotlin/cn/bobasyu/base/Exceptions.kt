package cn.bobasyu.base

/**
 * 基础异常封装，仅添加了错误码信息
 */
open class BaseException(
    val code: Int = ResultCode.INTERNAL_ERROR.code,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 数据库为查询到数据异常封装
 */
class NoSuchRecordInDatabaseException(
    message: String = "No such record in database.",
    cause: Throwable? = null
) : BaseException(ResultCode.INTERNAL_ERROR.code, message, cause)

class UnauthorizedException(
    message: String = "Unauthorized.",
    cause: Throwable? = null
) : BaseException(ResultCode.UNAUTHORIZED.code, message, cause)
