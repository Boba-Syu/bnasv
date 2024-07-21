package cn.bobasyu.databeses

import cn.bobasyu.utils.parseJson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.mysqlclient.mySQLConnectOptionsOf
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.*


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

    inline fun <reified T> query(sql: String, resultType: Class<T>): Future<List<T>> =
        sqlPool.withConnection { connection: SqlConnection ->
            connection.query(sql).execute().map { rowSet: RowSet<Row> ->
                rowSet.map { row -> row.toJson().toString().parseJson(resultType) }.toList()
            }
        }

    inline fun <reified T> queryByConditions(sql: String, conditions: List<Tuple>, resultType: Class<T>): Future<T> =
        sqlPool.withConnection { connection: SqlConnection ->
            connection.preparedQuery(sql)
                .executeBatch(conditions)
                .map { rowSet -> rowSet.first().toJson().toString().parseJson(resultType) }
                .onFailure { throw it }
        }

    fun insert(sql: String, insertData: List<Tuple>): Future<Unit> =
        sqlPool.withConnection { connection: SqlConnection ->
            connection.preparedQuery(sql)
                .executeBatch(insertData)
                .map { }
                .onFailure { throw it }
        }

}