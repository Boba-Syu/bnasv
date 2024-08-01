package cn.bobasyu.databeses

import cn.bobasyu.utils.camelToSnakeCase
import io.vertx.core.Future
import io.vertx.sqlclient.Tuple
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

enum class Order(val value: String) {
    ASC("ASC"), DESC("DESC")
}

enum class GenerateType {
    INSERT, UPDATE, DELETE, SELECT
}

/**
 * SQL语句生成
 */
class SqlGenerator(
    /**
     * sql操作的表对应的实体类类型
     */
    private val resultType: KClass<out Any>,
) {
    private val sql = StringBuilder()
    private val sqlConditionGenerator by lazy { SqlConditionGenerator(this) }
    private val params: MutableList<Any> = ArrayList()
    private lateinit var generateType: GenerateType

    fun generate(): String = sql.toString()

    fun execute(mySqlClient: MySqlClient): Future<out Any> {
        val condition = listOf(Tuple.from(params))

        return when (generateType) {
            GenerateType.SELECT -> mySqlClient.queryByConditions(sql.toString(), condition, resultType)
            GenerateType.INSERT -> mySqlClient.save(sql.toString(), condition)
            GenerateType.UPDATE -> mySqlClient.save(sql.toString(), condition)
            GenerateType.DELETE -> mySqlClient.save(sql.toString(), condition)
        }
    }

    /**
     * 查询语句
     *
     * @param typeList 查询语句返回结果中包含的字段类型
     */
    fun select(vararg typeList: KProperty<Any>): SqlGenerator = this.apply {
        generateType = GenerateType.SELECT
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
    fun select(): SqlGenerator = this.apply {
        sql.append("select\n\t*\nfrom ${resultType.simpleName!!.camelToSnakeCase()}\n")
    }

    /**
     * 生成插入语句
     *
     * @param typeList 被插入的字段
     */
    fun <U : Any> insert(vararg typeList: KProperty<U>): SqlInsertGenerator {
        generateType = GenerateType.INSERT
        val typeNameList: List<String> = typeList.map { it.name.camelToSnakeCase() }
        return insert(typeNameList)
    }

    /**
     * 插入语句，默认全部字段
     */
    fun insert(): SqlInsertGenerator {
        val typeNameList: List<String> = resultType.memberProperties.map { it.name.camelToSnakeCase() }
        return insert(typeNameList)
    }

    private fun insert(typeNameList: List<String>): SqlInsertGenerator {
        generateType = GenerateType.INSERT
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
        return SqlInsertGenerator(this)
    }

    /**
     * 更新语句
     *
     * @param typeList 被更新的字段
     */
    fun <U> update(vararg typeList: KProperty<U>): SqlGenerator = this.apply {
        generateType = GenerateType.UPDATE
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
    fun delete(): SqlGenerator = this.apply {
        generateType = GenerateType.DELETE
        sql.append("delete from ${resultType.simpleName!!.camelToSnakeCase()}\n")
    }

    /**
     * sql语句中拼接 where，配合 select、update或delete 使用
     */
    fun where(): SqlConditionGenerator {
        sql.append("where\n")
        return sqlConditionGenerator
    }

    class SqlConditionGenerator(
        private val sqlGenerator: SqlGenerator,
    ) {
        private val sql: StringBuilder = sqlGenerator.sql
        private val sqlWhereGenerator by lazy { SqlWhereGenerator(this, sqlGenerator) }

        /**
         * sql语句中拼接 等于 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> eq(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} = ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }

        /**
         * sql语句中拼接 不等于 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> neq(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} != ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }

        /**
         * sql语句中拼接 大于 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> gt(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} > ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }

        /**
         * sql语句中拼接 小于 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> lt(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} < ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }

        /**
         * sql语句中拼接 大于等于 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> gte(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} >= ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }

        /**
         * sql语句中拼接 小于等于 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> lte(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} <= ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }

        /**
         * sql语句中拼接 模糊查询like 条件，配合 where 使用
         *
         * @param type 查询条件对应的字段类型
         */
        fun <U : Any> like(type: KProperty<U>, obj: U): SqlWhereGenerator {
            sql.append("\t${type.name.camelToSnakeCase()} like ?\n")
            sqlGenerator.params.add(obj)
            return sqlWhereGenerator
        }
    }

    class SqlWhereGenerator(
        private val sqlConditionGenerator: SqlConditionGenerator,
        private val sqlGenerator: SqlGenerator,
    ) : SqlEndGenerator(sqlGenerator) {
        private val sql: StringBuilder = sqlGenerator.sql

        /**
         * sql语句中拼接 and，配合 where 使用
         */
        fun and(): SqlConditionGenerator {
            sql.append("and\n")
            return sqlConditionGenerator
        }

        /**
         * sql语句中拼接 or，配合 where 使用
         */
        fun or(): SqlConditionGenerator {
            sql.append("or\n")
            return sqlConditionGenerator
        }

        fun <U : Any> orderBy(type: KProperty<U>, order: Order): SqlEndGenerator {
            sql.append("order by ${type.name.camelToSnakeCase()} ${order.value}\n")
            return SqlEndGenerator(sqlGenerator)
        }
    }

    class SqlInsertGenerator(
        private val sqlGenerator: SqlGenerator
    ) : SqlEndGenerator(sqlGenerator) {
        fun values(vararg values: Any): SqlInsertGenerator = this.apply {
            with(sqlGenerator.sql) {
                append("(")
                values.forEach { value ->
                    sqlGenerator.params.add(value)
                    append("?")
                    if (value != values.last()) {
                        append(",")
                    }
                }
                append(")\n")
            }
        }
    }

    open class SqlEndGenerator(
        private val sqlGenerator: SqlGenerator
    ) {
        private val sql: StringBuilder = sqlGenerator.sql

        fun generate(): String = sqlGenerator.generate()

        fun execute(mySqlClient: MySqlClient): Future<out Any> = sqlGenerator.execute(mySqlClient)
    }
}