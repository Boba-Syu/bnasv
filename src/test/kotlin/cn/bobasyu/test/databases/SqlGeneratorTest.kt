package cn.bobasyu.test.databases

import cn.bobasyu.databeses.Order
import cn.bobasyu.databeses.SqlGenerator
import cn.bobasyu.user.UserRecord
import io.vertx.junit5.VertxExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(VertxExtension::class)
class SqlGeneratorTest {

    @Test
    fun selectGenerateTest1() {
        val sql = SqlGenerator(UserRecord::class)
            .select()
            .generate()
        assertEquals(sql, "SELECT * FROM user_record;")
    }

    @Test
    fun selectGenerateTest2() {
        val sql = SqlGenerator(UserRecord::class)
            .select(UserRecord::userId, UserRecord::password)
            .generate()
        assertEquals(sql, "SELECT user_id, password FROM user_record;")
    }

    @Test
    fun selectGenerateTest3() {
        val sql = SqlGenerator(UserRecord::class)
            .select()
            .where()
            .eq(UserRecord::userId, 1)
            .generate()
        assertEquals(sql, "SELECT * FROM user_record WHERE user_id = ?;")
    }

    @Test
    fun selectGenerateTest4() {
        val sql: String = SqlGenerator(UserRecord::class)
            .select()
            .where()
            .gt(UserRecord::userId, 1)
            .and()
            .neq(UserRecord::username, "testUsername")
            .generate()
        assertEquals(sql, "SELECT * FROM user_record WHERE user_id > ? AND username != ?;")

    }

    @Test
    fun selectGenerateTest5() {
        val sql: String = SqlGenerator(UserRecord::class)
            .select()
            .where()
            .like(UserRecord::userId, 1)
            .orderBy(UserRecord::userId, Order.ASC)
            .generate()
        assertEquals(sql, "SELECT * FROM user_record WHERE user_id like ? ORDER BY user_id ASC;")
    }

    @Test
    fun insertGenerateTest1() {
        val sql: String = SqlGenerator(UserRecord::class)
            .insert()
            .values(1, "testUsername", "testPassword", LocalDateTime.now(), LocalDateTime.now())
            .generate()
        assertEquals(
            sql,
            "INSERT INTO user_record(user_id, username, password, create_time, update_time) VALUES (?, ?, ?, ?, ?);"
        )
    }

    @Test
    fun insertGenerateTest2() {
        val sql: String = SqlGenerator(UserRecord::class)
            .insert(UserRecord::username, UserRecord::password)
            .values("testUsername", "testPassword")
            .generate()
        assertEquals(sql, "INSERT INTO user_record(username, password) VALUES (?, ?);")
    }

    @Test
    fun updateGenerateTest1() {
        val sql: String = SqlGenerator(UserRecord::class)
            .update(UserRecord::username)
            .generate()
        assertEquals(sql, "UPDATE user_record SET username = ?;")
    }

    @Test
    fun updateGenerateTest2() {
        val sql: String = SqlGenerator(UserRecord::class)
            .update(UserRecord::username)
            .where()
            .eq(UserRecord::username, "testUsername")
            .generate()
        assertEquals(sql, "UPDATE user_record SET username = ? WHERE username = ?;")
    }

    @Test
    fun deleteGenerateTest1() {
        val sql:String = SqlGenerator(UserRecord::class)
            .delete()
            .where()
            .eq(UserRecord::username, "testUsername")
            .generate()
        assertEquals(sql, "DELETE FROM user_record WHERE username = ?;")
    }
}