package cn.bobasyu.databeses

import cn.bobasyu.entity.UserRecord
import cn.bobasyu.entity.UserRecords
import io.vertx.core.Vertx
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.BaseTable


class DatabaseHandler(
    val vertx: Vertx
) {
    val database: Database =
        Database.connect("jdbc:postgresql://localhost/bnasv", user = "postgres", password = "zhoubo")

    fun <T : BaseTable<*>> from(table: BaseTable<T>) = database.from(table)

    fun <T : BaseTable<*>> update(table: T, block: UpdateStatementBuilder.(T) -> Unit) = database.update(table, block)

    fun <T : BaseTable<*>> insert(table: T, block: AssignmentsBuilder.(T) -> Unit) = database.insert(table, block)
}

fun main() {
    val database = Database.connect("jdbc:postgresql://localhost/bnasv", user = "postgres", password = "zhoubo")

    for (row in database.from(UserRecords).select()) {
        println(row[UserRecords.username])
    }
    val userRecord: UserRecord? = database.sequenceOf(UserRecords).find { it.userId eq 1 }
    println(userRecord)
}