package cn.bobasyu.user

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseException
import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_INSERT_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_BY_ID_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT
import cn.bobasyu.constant.UserRepositoryConsumerConstant.USER_QUERY_EVENT
import cn.bobasyu.utils.generateId
import io.vertx.core.eventbus.EventBus
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
) : BaseRepositoryVerticle(applicationContext) {
    private val eventBus: EventBus by lazy { vertx.eventBus() }
    private val databaseHandler = applicationContext.databaseHandler

    /**
     * 注册总线事件消费方法
     */
    override fun registerConsumer() = with(eventBus) {
        asyncConsumer(USER_QUERY_EVENT, ::queryUserList)
        asyncConsumer(USER_QUERY_BY_ID_EVENT, ::queryUserById)
        asyncConsumer(USER_INSERT_EVENT, ::insertUser)
        asyncConsumer(USER_QUERY_BY_USERNAME_AND_PASSWORD_EVENT, ::queryUserByUsernameAndPassword)
    }

    override suspend fun start() {
        super.start()
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
