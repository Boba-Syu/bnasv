package cn.bobasyu.databeses

import cn.bobasyu.utils.parseJson
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.mysqlclient.mySQLConnectOptionsOf
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
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

    inline fun <reified T> query(sql: String, resultType: Class<T>): Future<List<T>> =
        sqlPool.withConnection { connection ->
            connection.query(sql).execute().map { rowSet ->
                rowSet.map { row -> row.toJson().toString().parseJson(resultType) }.toList()
            }
        }

    inline fun <reified T> queryByCondition(sql: String, conditions: Tuple, resultType: Class<T>): Future<T> =
        sqlPool.withConnection { connection ->
            connection.preparedQuery(sql)
                .execute(conditions)
                .map { rowSet -> rowSet.first().toJson().toString().parseJson(resultType) }
                .onFailure { throw it }
        }
}