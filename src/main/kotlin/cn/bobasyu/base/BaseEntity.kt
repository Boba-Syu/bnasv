package cn.bobasyu.base

import java.io.Serializable

class PageInfo<T>(
    val list: List<T>,
    val total: Int
) : Serializable

class PageVal(
    val pageSize: Int,
    val pageNum: Int,
) : Serializable


class PageList<T>(
    val total: Int,
) : ArrayList<T>()

fun <T> pageListOf(total: Int, vararg args: T): PageList<T> = PageList<T>(total).apply {
    args.forEach { this.add(it) }
}
fun <T> pageListOf(total: Int, args: Collection<T>): PageList<T> = PageList<T>(total).apply {
    args.forEach { this.add(it) }
}

