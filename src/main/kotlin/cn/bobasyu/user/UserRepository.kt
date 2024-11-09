package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.entity.UserInsertDTO
import cn.bobasyu.entity.UserLoginDTO
import cn.bobasyu.entity.UserRecord
import cn.bobasyu.entity.userRecords
import cn.bobasyu.utils.generateId
import io.vertx.core.eventbus.Message
import org.ktorm.dsl.eq
import org.ktorm.dsl.neq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.time.LocalDateTime


/**
 * AbstractUserRepository使用vertx-mysql的具体实现
 */
open class UserRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractUserRepository(applicationContext) {
    private val databaseHandler = applicationContext.databaseHandler

    override suspend fun start() {
        super.start()
    }

    override suspend fun handleQueryUserListEvent(message: Message<Unit>) = handleEvent(message) {
        queryUserList()
    }

    override suspend fun handleQueryUserByIdEvent(message: Message<Long>) = handleEvent(message) {
        val userId = message.body()
        queryUserById(userId)
        SUCCESS
    }

    override suspend fun handleInsertUserEvent(message: Message<UserInsertDTO>) = handleEvent(message) {
        val userInsertDTO: UserInsertDTO = message.body()
        val ifExisted = queryUsernameExist(userInsertDTO.username)
        if (ifExisted) {
            throw BaseException(message = "username${userInsertDTO.username} is existed")
        }
        insertUser(userInsertDTO)
        SUCCESS
    }

    override suspend fun handleQueryUserByUsernameAndPasswordEvent(message: Message<UserLoginDTO>) =
        handleEvent(message) {
            val userLoginDTO: UserLoginDTO = message.body()
            queryUserByUsernameAndPassword(userLoginDTO)
        }

    private fun queryUsernameExist(username: String): Boolean {
        val userRecord: UserRecord? = databaseHandler.userRecords.find { it.username eq username }
        return userRecord != null
    }

    private fun queryUserList(): List<UserRecord> {
        val list: List<UserRecord> = databaseHandler.userRecords.filter { it.userId neq 0 }.toList()
        return list
    }

    private fun queryUserById(id: Long): UserRecord {
        val userRecord: UserRecord? = databaseHandler.userRecords.find { it.userId eq id }
        if (userRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return userRecord
    }

    private fun queryUserByUsername(username: String): UserRecord {
        val userRecord: UserRecord? = databaseHandler.userRecords.find { it.username eq username }
        if (userRecord == null) {
            throw NoSuchRecordInDatabaseException("username: $username")
        }
        return userRecord
    }

    private fun insertUser(userInsertDTO: UserInsertDTO) {
        val userRecord = UserRecord {
            userId = generateId()
            username = userInsertDTO.username
            password = userInsertDTO.password
            otherProperties = hashMapOf()
            createTime = LocalDateTime.now()
            updateTime = LocalDateTime.now()
        }
        databaseHandler.userRecords.add(userRecord)
    }

    private fun queryUserByUsernameAndPassword(userLoginDTO: UserLoginDTO): UserRecord {
        val userRecord: UserRecord? = databaseHandler.userRecords.find {
            it.username eq userLoginDTO.username
            it.password eq userLoginDTO.password
        }
        if (userRecord == null) {
            throw BaseException(message = "username or password error")
        }
        return userRecord
    }
}
