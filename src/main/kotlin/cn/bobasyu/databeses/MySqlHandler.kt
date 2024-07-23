package cn.bobasyu.databeses

import cn.bobasyu.utils.parseJson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.mysqlclient.mySQLConnectOptionsOf
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple


class MySqlClient(
    private val vertx: Vertx,
) {
    val sqlPool: MySQLPool by lazy { initSqlClient() }

    private fun initSqlClient(): MySQLPool {
        val collectionOptions: MySQLConnectOptions = mySQLConnectOptionsOf(
            port = 3306,
            host = "localhost",
            database = "bnasv",
            user = "root",
            password = "123456",
        )
        val poolOptions: PoolOptions = poolOptionsOf(
            maxSize = 10
        )
        return MySQLPool.pool(vertx, collectionOptions, poolOptions)
    }

    fun close(): Future<Void> = sqlPool.close()

    inline fun <reified T> query(sql: String, resultType: Class<T>): Future<List<T>> = sqlPool.withConnection {
        it.query(sql, resultType)
    }

    inline fun <reified T> queryByConditions(sql: String, conditions: List<Tuple>, resultType: Class<T>): Future<List<T>> =
        sqlPool.withConnection { it.queryByConditions(sql, conditions, resultType) }

    fun save(sql: String, insertData: List<Tuple>): Future<Unit> = sqlPool.withConnection { it.save(sql, insertData) }

    inline fun <reified T> withTransaction(crossinline callback: (SqlConnection) -> Future<T>): Future<T> =
       sqlPool.withTransaction { connection: SqlConnection -> callback(connection) }
}

inline fun <reified T> SqlConnection.query(sql: String, resultType: Class<T>): Future<List<T>> {
    return query(sql).execute().map { rowSet: RowSet<Row> ->
        rowSet.map { row -> row.toJson().toString().parseJson(resultType) }.toList()
    }
}

inline fun <reified T> SqlConnection.queryByConditions(sql: String, conditions: List<Tuple>, resultType: Class<T>): Future<List<T>> {
    return preparedQuery(sql)
        .executeBatch(conditions)
        .map { rowSet ->
            rowSet.map { it.toJson().toString().parseJson(resultType) }
        }.onFailure { throw it }
}

fun SqlConnection.save(sql: String, insertData: List<Tuple>): Future<Unit> {
    return preparedQuery(sql)
        .executeBatch(insertData)
        .map { }
        .onFailure { throw it }
}
