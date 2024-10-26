package cn.bobasyu.databeses

import cn.bobasyu.utils.camelToSnakeCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * 另一种Sql生成封装
 */
typealias SqlElement = () -> String


object WhereGenerator {

    fun where(condition: SqlElement): SqlElement = { "WHERE ${condition()}" }

    inline fun <reified T : Any> eq(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} = '$value' " }
    }

    inline fun <reified T : Any> neq(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} != '$value' " }
    }

    inline fun <reified T : Any> gt(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} > '$value' " }
    }

    inline fun <reified T : Any> lt(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} < '$value' " }
    }

    inline fun <reified T : Any> gte(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} >= '$value' " }
    }

    inline fun <reified T : Any> lte(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} <= '$value' " }
    }

    inline fun <reified T : Any> like(column: KProperty<T>, value: T): SqlElement {
        return { "${column.name.camelToSnakeCase()} like '$value' " }
    }

    inline fun <reified T : Any> orderBy(column: KProperty<T>, order: Order = Order.ASC): SqlElement {
        return { "ORDER BY ${column.name.camelToSnakeCase()} ${order.value} " }
    }

    infix fun SqlElement.and(other: SqlElement): SqlElement = { "${this()}AND ${other()}" }

    infix fun SqlElement.or(other: SqlElement): SqlElement = { "${this()}OR ${other()}" }
}

object SelectGenerator {

    fun <T : Any> select(type: KClass<T>, vararg fn: SqlElement): String = with(StringBuilder()) {
        append("SELECT * ")
        append("FROM ${type.simpleName!!.camelToSnakeCase()} ")
        fn.forEach { append(it()) }
        append(";")
        toString()
    }

    fun select(vararg fn: SqlElement): String = with(StringBuilder()) {
        append("SELECT ")
        fn.forEach { append(it()) }
        append(";")
        toString()
    }

    infix fun List<KProperty<*>>.from(tableName: KClass<*>): SqlElement {
        return {
            val stringBuilder = StringBuilder()
            this.forEach { property ->
                stringBuilder.append(property.name.camelToSnakeCase())
                when {
                    property != this.last() -> stringBuilder.append(", ")
                    else -> stringBuilder.append(" ")
                }
            }
            stringBuilder.append("FROM ${tableName.simpleName!!.camelToSnakeCase()} ")
            stringBuilder.toString()
        }
    }
}

object InsertGenerator {

    fun <T : Any> insertInto(type: KClass<T>, vararg fn: SqlElement): String = with(StringBuilder()) {
        append("INSERT INTO ${type.simpleName!!.camelToSnakeCase()} ")
        fn.forEach { append(it()) }
        append(";")
        toString()
    }

    fun insertInto(vararg fn: SqlElement): String = with(StringBuilder()) {
        append("INSERT INTO ")
        fn.forEach { append(it()) }
        append(";")
        toString()
    }

    fun values(vararg saveValues: List<Any>): SqlElement {
        return {
            with(StringBuilder()) {
                append("VALUES ")
                saveValues.forEach { list: List<Any> ->
                    append("(")
                    list.forEach {
                        append("'$it'")
                        when {
                            it != list.last() -> append(", ")
                            else -> append(")")
                        }
                    }
                    when {
                        list != saveValues.last() -> append(", ")
                        else -> append(" ")
                    }
                }
                toString()
            }
        }
    }

    inline operator fun <reified T : Any, U> KClass<T>.get(vararg properties: KProperty<U>): SqlElement {
        return {
            val stringBuilder = StringBuilder()
            stringBuilder.append("${this.simpleName!!.camelToSnakeCase()}(")
            with(stringBuilder) {
                properties.forEach { property: KProperty<U> ->
                    append(property.name.camelToSnakeCase())
                    if (property != properties.last()) {
                        append(", ")
                    }
                }
                append(") ")
                toString()
            }
        }
    }
}

object UpdateGenerator {
    fun <T : Any> update(type: KClass<T>, vararg fn: SqlElement): String = with(StringBuilder()) {
        append("UPDATE ${type.simpleName!!.camelToSnakeCase()} ")
        fn.forEach { append(it()) }
        append(";")
        toString()
    }

    fun <T> set(vararg pairs: Pair<KProperty<T>, T>): SqlElement {
        return {
            StringBuilder().apply {
                append("SET ")
                pairs.forEach { pair ->
                    append("${pair.first.name.camelToSnakeCase()} = '${pair.second}'")
                    when {
                        pair != pairs.last() -> append(", ")
                        else -> append(" ")
                    }
                }
            }.toString()
        }
    }
}

object DeleteGenerator {
    fun <T : Any> delete(type: KClass<T>, vararg fn: SqlElement): String = with(StringBuilder()) {
        append("DELETE FROM ${type.simpleName!!.camelToSnakeCase()} ")
        fn.forEach { append(it()) }
        append(";")
        toString()
    }
}
