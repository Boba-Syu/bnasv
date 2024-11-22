package cn.bobasyu.note

import cn.bobasyu.base.*
import cn.bobasyu.constant.NoteParamConstant
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_PAGE_INFO
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_QUERY_BY_ID_EVENT
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_UPDATE_EVENT
import cn.bobasyu.entity.*
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.coAwait

class NoteHandler(
    private val applicationContext: ApplicationContext
) : BaseServiceVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    override fun setUserRouter() = with(applicationContext.router) {
        get("/note").coroutineHandler { queryById(it) }
        post("/note").coroutineHandler { updateNote(it) }
        post("/note/page").coroutineHandler { pageInfo(it) }
    }

    private fun updateNote(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            val json = body.toString()
            val noteDto: NoteDto = json.parseJson(NoteDto::class.java)
            eventBus.request<String>(NOTE_UPDATE_EVENT, noteDto)
                .onSuccess { ctx.response().end(success().toJson()) }
                .onFailure { ctx.response().end(failure(it.message).toJson()) }
        }
    }

    private suspend fun queryById(ctx: RoutingContext) {
        val noteId: Long = ctx.request().getParam(NoteParamConstant.NOTE_ID).toLong()
        val resp: Message<NoteRecord> = eventBus.request<NoteRecord>(NOTE_QUERY_BY_ID_EVENT, noteId).coAwait()
        val result = json {
            obj { NoteParamConstant.NOTE_ID to resp.body().noteId }
            obj { NoteParamConstant.TITLE to resp.body().title }
            obj { NoteParamConstant.CONTENT to resp.body().content }
            obj { NoteParamConstant.OTHER_PROPERTIES to resp.body().otherProperties }
            obj { NoteParamConstant.CREATE_TIME to resp.body().createTime }
            obj { NoteParamConstant.UPDATE_TIME to resp.body().updateTime }
        }
        ctx.response().end(success(result).toJson())
    }

    private fun pageInfo(ctx: RoutingContext) {
        ctx.request().asyncRequestBodyHandler(ctx) { body: Buffer ->
            val notePageDto: NotePageDto = body.toString().parseJson(NotePageDto::class.java)
            val resp: Message<PageInfo<NoteRecord>> =
                eventBus.request<PageInfo<NoteRecord>>(NOTE_PAGE_INFO, notePageDto)
                    .coAwait()
            val pageInfo = PageInfo(resp.body().list.map {
                json {
                    obj { NoteParamConstant.NOTE_ID to it.noteId }
                    obj { NoteParamConstant.TITLE to it.title }
                    obj { NoteParamConstant.CONTENT to it.content }
                    obj { NoteParamConstant.OTHER_PROPERTIES to it.otherProperties }
                    obj { NoteParamConstant.CREATE_TIME to it.createTime }
                    obj { NoteParamConstant.UPDATE_TIME to it.updateTime }
                }
            }, resp.body().total)
            ctx.response().end(success(pageInfo).toJson())

        }
    }
}

abstract class AbstractNoteRepository(
    applicationContext: ApplicationContext
) : BaseRepositoryVerticle(applicationContext) {

    override fun registerConsumer() = with(vertx.eventBus()) {
        asyncConsumer(NOTE_QUERY_BY_ID_EVENT) { handleQueryByIdEvent(it) }
        asyncConsumer(NOTE_UPDATE_EVENT) { handleUpdateEvent(it) }
        asyncConsumer(NOTE_PAGE_INFO) { handlePageInfoEvent(it) }
    }

    /**
     * 根据Id查询事件
     */
    abstract suspend fun handleQueryByIdEvent(message: Message<Long>)

    /**
     * 更新事件
     */
    abstract suspend fun handleUpdateEvent(message: Message<NoteDto>)

    /**
     * 分页查询
     */
    abstract suspend fun handlePageInfoEvent(message: Message<NotePageDto>)
}

/**
 * 笔记相关的服务注册
 */
fun Vertx.deployNoteVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(NoteHandler(applicationContext))
    deployVerticle(NoteRepositoryVerticle(applicationContext))
}
