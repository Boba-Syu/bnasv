package cn.bobasyu.databeses

import cn.bobasyu.user.UserRecord
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

/**
 * SQL语句生成
 */
class SqlGenerator<T : Any>(
    /**
     * sql操作的表对应的实体类类型
     */
    private val resultType: KClass<T>,
) {
    private val sql = StringBuilder()

    fun execute(): String = sql.toString()

    /**
     * 查询语句
     *
     * @param typeList 查询语句返回结果中包含的字段类型
     */
    fun select(vararg typeList: KProperty<Any>): SqlGenerator<T> = this.apply {
        with(sql) {
            append("select\n")
            typeList.forEach { type ->
                append("\t${type.name.camelToSnakeCase()}")
                if (typeList.last() != type) {
                    append(",\n")
                }
            }
            append("\nfrom ${resultType.simpleName!!.camelToSnakeCase()}\n")
        }
    }

    /**
     * 查询语句，默认全部字段
     */
    fun select() = this.apply { sql.append("select\n\t*\nfrom ${resultType.simpleName!!.camelToSnakeCase()}\n") }

    /**
     * sql语句中拼接 where，配合 select、update或delete 使用
     */
    fun where(): SqlGenerator<T> = this.apply { sql.append("where\n") }

    /**
     * sql语句中拼接 and，配合 where 使用
     */
    fun and(): SqlGenerator<T> = this.apply { sql.append("and\n") }

    /**
     * sql语句中拼接 or，配合 where 使用
     */
    fun or(): SqlGenerator<T> = this.apply { sql.append("or\n") }

    /**
     * sql语句中拼接 等于 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> eq(type: KProperty<U>): SqlGenerator<T> = this.apply {
        sql.append("\t${type.name.camelToSnakeCase()} = ?\n")
    }

    /**
     * sql语句中拼接 不等于 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> neq(type: KProperty<U>): SqlGenerator<T> = this.apply {
        sql.append("\t${type.name.camelToSnakeCase()} != ?\n")
    }

    /**
     * sql语句中拼接 大于 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> gt(type: KProperty<U>): SqlGenerator<T> = this.apply {
        sql.append("\t${type.name.camelToSnakeCase()} > ?\n")
    }

    /**
     * sql语句中拼接 小于 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> lt(type: KProperty<U>): SqlGenerator<T> = this.apply {
        sql.append("\t${type.name.camelToSnakeCase()} < ?\n")
    }

    /**
     * sql语句中拼接 大于等于 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> gte(type: KProperty<U>): SqlGenerator<T> = this.apply {
        sql.append("\t${type.name.camelToSnakeCase()} >= ?\n")
    }

    /**
     * sql语句中拼接 小于等于 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> lte(type: KProperty<U>): SqlGenerator<T> = this.apply {
        sql.append("\t${type.name.camelToSnakeCase()} <= ?\n")
    }

    /**
     * sql语句中拼接 模糊查询like 条件，配合 where 使用
     *
     * @param type 查询条件对应的字段类型
     */
    fun <U : Any> like(type: KProperty<U>): SqlGenerator<T> {
        return this.apply {
            sql.append("\t${type.name.camelToSnakeCase()} like ?\n")
        }
    }

    /**
     * 生成插入语句
     *
     * @param typeList 被插入的字段
     */
    fun <U : Any> insert(vararg typeList: KProperty<U>): SqlGenerator<T> {
        val typeNameList: List<String> = typeList.map { it.name.camelToSnakeCase() }
        return insert(typeNameList)
    }

    /**
     * 插入语句，默认全部字段
     */
    fun insert(): SqlGenerator<T> {
        val typeNameList: List<String> = resultType.memberProperties.map { it.name }
        return insert(typeNameList)
    }

    private fun insert(typeNameList: List<String>) = this.apply {
        with(sql) {
            append("insert into ${resultType.simpleName!!.camelToSnakeCase()}(\n")
            typeNameList.forEach { typeName ->
                append("\t${typeName.camelToSnakeCase()}")
                if (typeNameList.last() != typeName) {
                    append(",\n")
                }
            }
            append("\n) values(\n")
            for (i in 1..typeNameList.size) {
                append("\t?")
                if (i != typeNameList.size) {
                    append(",\n")
                }
            }
            append("\n)")
        }
    }

    /**
     * 更新语句
     *
     * @param typeList 被更新的字段
     */
    fun <U> update(vararg typeList: KProperty<U>) = this.apply {
        with(sql) {
            append("update ${resultType.simpleName!!.camelToSnakeCase()}\nset\n")
            typeList.forEach { type ->
                append("\t${type.name} = ?")
                if (typeList.last() != type) {
                    append(",\n")
                }
            }
            append("\n")
        }
    }

    /**
     * 删除语句
     */
    fun delete() = this.apply {
        sql.append("delete from ${resultType.simpleName!!.camelToSnakeCase()}\n")
    }
}

fun String.camelToSnakeCase(): String {
    return this.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
        .removePrefix("_")
        .lowercase()
}


fun main() {
    var sql = SqlGenerator(UserRecord::class)
        .select(UserRecord::userId, UserRecord::username, UserRecord::password)
        .where()
        .lt(UserRecord::userId)
        .and()
        .like(UserRecord::username)
        .or()
        .eq(UserRecord::password)
        .execute()
    println(sql)
    println("-----")
    sql = SqlGenerator(UserRecord::class).select().execute()
    println(sql)
    println("-----")
    sql = SqlGenerator(UserRecord::class).insert().execute()
    println(sql)
    println("-----")
    sql = SqlGenerator(UserRecord::class)
        .update(UserRecord::username)
        .where()
        .gte(UserRecord::userId)
        .execute()
    println(sql)
    println("-----")
    sql = SqlGenerator(UserRecord::class)
        .delete()
        .where()
        .lte(UserRecord::userId)
        .or()
        .neq(UserRecord::userId)
        .or()
        .gt(UserRecord::userId)
        .or()
        .like(UserRecord::username)
        .execute()
    println(sql)
    println("-----")
}