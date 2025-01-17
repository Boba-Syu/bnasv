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