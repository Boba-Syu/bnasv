package cn.bobasyu.base

/**
 * 响应码
 */
enum class ResultCode(
    val code: Int,
) {
    SUCCESS(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    INTERNAL_ERROR(500)
}

/**
 * http相应封装
 */
data class HttpResult<T>(
    val code: Int,
    val data: T? = null,
    val message: String? = null,
)

fun <T> success(data: T): HttpResult<T> = HttpResult(code = ResultCode.SUCCESS.code, data = data)

fun success(): HttpResult<Unit> = HttpResult(code = ResultCode.SUCCESS.code)

fun failure(message: String?): HttpResult<Unit> = HttpResult(code = ResultCode.INTERNAL_ERROR.code, message = message)

fun failure(e: Throwable): HttpResult<Unit> = when (e) {
    is BaseException -> HttpResult(code = e.code, message = e.message)
    else -> HttpResult(code = ResultCode.INTERNAL_ERROR.code, message = e.message)
}

fun notFound(uri: String): HttpResult<Unit> =
    HttpResult(code = ResultCode.NOT_FOUND.code, message = "not found uri  [${uri}].")