package cn.bobasyu.databeses

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.ConfigName
import io.vertx.core.Vertx
import org.ktorm.database.Database
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.UpdateStatementBuilder
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.schema.BaseTable
import org.ktorm.support.postgresql.InsertOrUpdateStatementBuilder
import org.ktorm.support.postgresql.insertOrUpdate

@ConfigName("database")
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
)

class DatabaseHandler(
    applicationContext: ApplicationContext
) {
    private val databaseConfig: DatabaseConfig = applicationContext.config[DatabaseConfig::class]
    private val url get() = databaseConfig.url
    private val user get() = databaseConfig.user
    private val password get() = databaseConfig.password

    val database: Database = Database.connect(url, user = user, password = password)

    fun <T : BaseTable<*>> from(table: T) = database.from(table)

    fun <T : BaseTable<*>> insertOrUpdate(table: T, block: InsertOrUpdateStatementBuilder.(T) -> Unit) = database.insertOrUpdate(table, block)

    fun <T : BaseTable<*>> update(table: T, block: UpdateStatementBuilder.(T) -> Unit) = database.update(table, block)

    fun <T : BaseTable<*>> insert(table: T, block: AssignmentsBuilder.(T) -> Unit) = database.insert(table, block)
}