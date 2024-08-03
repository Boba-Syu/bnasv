package cn.bobasyu.user

import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await


/**
 * AbstractUserRepository使用vertx-mysql的具体实现
 */
class UserRepositoryVerticle(
    private val mySqlClient: MySqlClient,
) : AbstractUserRepository() {

    override suspend fun queryUserList(): Future<List<UserRecord>> {
        val queryListSql: String = SqlGenerator(UserRecord::class).select().generate()
        return mySqlClient.query(queryListSql, UserRecord::class.java)
    }

    override suspend fun queryUserById(id: Int): Future<UserRecord> {
        return SqlGenerator(UserRecord::class)
            .select()
            .where().eq(UserRecord::userId, id)
            .execute(mySqlClient)
            .map {
                if ((it as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException("id: $id")
                }
                it.first()
            }
            .map { it as UserRecord }
    }

    override suspend fun queryUserByUsername(username: String): Future<UserRecord> {
        return SqlGenerator(UserRecord::class)
            .select()
            .where().eq(UserRecord::username, username)
            .execute(mySqlClient)
            .map {
                if ((it as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException("username: $username")
                }
                it.first()
            }
            .map { it as UserRecord }
    }


    override suspend fun insertUser(userInsertDTO: UserInsertDTO): Future<Unit> {
        try {
            queryUserByUsername(userInsertDTO.username).await()
            throw BaseException(message = "username${userInsertDTO.username} is existed")
        } catch (_: NoSuchRecordInDatabaseException) {
            return SqlGenerator(UserRecord::class)
                .insert(UserRecord::username, UserRecord::password)
                .values(userInsertDTO.username, userInsertDTO.password)
                .execute(mySqlClient)
                .map {}
        }
    }


    override suspend fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): Future<UserRecord> {
        return SqlGenerator(UserRecord::class).select()
            .where().eq(UserRecord::username, userLoginDTO.userName)
            .and().eq(UserRecord::password, userLoginDTO.password)
            .execute(mySqlClient)
            .map { it as UserRecord }
    }
}
