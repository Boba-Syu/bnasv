package cn.bobasyu.note

import cn.bobasyu.base.*
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_QUERY_BY_ID_EVENT
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_UPDATE_EVENT
import cn.bobasyu.entity.NoteDto
import cn.bobasyu.entity.NoteRecord
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await

class NoteHandler(
    private val applicationContext: ApplicationContext
) : BaseServiceVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    override fun setUserRouter() = with(applicationContext.router) {
        route("/note/*").authHandler()
        get("/note/:id").coroutineHandler { queryById(it) }
        post("/note").coroutineHandler { updateNote(it) }
    }

    private fun updateNote(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            val json = body.toString()
            val noteRecord: NoteRecord = json.parseJson(NoteRecord::class.java)
            eventBus.request<String>(NOTE_UPDATE_EVENT, noteRecord)
                .onSuccess { ctx.response().end(success().toJson()) }
                .onFailure { ctx.response().end(failure(it.message).toJson()) }
        }
    }

    private suspend fun queryById(ctx: RoutingContext) {
        val noteId: Int = ctx.request().getParam("noteId").toInt()
        val resp: Message<NoteRecord> = eventBus.request<NoteRecord>(NOTE_QUERY_BY_ID_EVENT, noteId).await()
        ctx.response().end(success(resp.body()).toJson())
    }
}

abstract class AbstractNoteRepository(
    applicationContext: ApplicationContext
) : BaseRepositoryVerticle(applicationContext) {

    override fun registerConsumer() = with(vertx.eventBus()) {
        asyncConsumer(NOTE_QUERY_BY_ID_EVENT) { handleQueryByIdEvent(it) }
        asyncConsumer(NOTE_UPDATE_EVENT) { handleUpdateEvent(it) }
    }

    /**
     * 根据Id查询事件
     */
    abstract suspend fun handleQueryByIdEvent(message: Message<Long>)

    /**
     * 更新事件
     */
    abstract suspend fun handleUpdateEvent(message: Message<NoteDto>)
}

/**
 * 笔记相关的服务注册
 */
fun Vertx.deployNoteVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(NoteHandler(applicationContext))
    deployVerticle(NoteRepositoryVerticle(applicationContext))
}
