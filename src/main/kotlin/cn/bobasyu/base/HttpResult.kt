package cn.bobasyu.base

data class HttpResult<T>(
    val code: Int,
    val data: T? = null,
    val message: String? = null,
)

fun <T> success(data: T): HttpResult<T> = HttpResult(code = 200, data = data)

fun success(): HttpResult<Unit> = HttpResult(code = 200)

fun failure(message: String?): HttpResult<Unit> = HttpResult(code = 500, message = message)

fun failure(exception: Throwable): HttpResult<Unit> = HttpResult(code = 500, message = exception.message)