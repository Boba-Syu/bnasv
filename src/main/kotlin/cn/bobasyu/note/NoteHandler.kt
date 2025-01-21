package cn.bobasyu.note

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseServiceVerticle
import cn.bobasyu.base.PageInfo
import cn.bobasyu.base.PageList
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_PAGE_INFO
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_QUERY_BY_ID_EVENT
import cn.bobasyu.constant.NoteRepositoryConsumerConstant.NOTE_UPDATE_EVENT
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.coAwait

class NoteHandler(
    private val applicationContext: ApplicationContext
) : BaseServiceVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    override fun setUserRouter() = with(applicationContext.router) {
        get("/note").doHandler(::queryById)
        post("/note").doHandler(::updateNote)
        post("/note/page").doHandler(::pageInfo)
    }

    private suspend fun updateNote(noteDto: NoteDto): String {
        val message = eventBus.request<String>(NOTE_UPDATE_EVENT, noteDto).coAwait()
        return message.body()
    }

    private suspend fun queryById(noteId: Long): JsonObject {
        val resp: Message<NoteRecord> = eventBus.request<NoteRecord>(NOTE_QUERY_BY_ID_EVENT, noteId).coAwait()
        return json {
            obj(
                NoteParamConstant.NOTE_ID to resp.body().noteId,
                NoteParamConstant.TITLE to resp.body().title,
                NoteParamConstant.CONTENT to resp.body().content,
                NoteParamConstant.OTHER_PROPERTIES to resp.body().otherProperties,
                NoteParamConstant.CREATE_TIME to resp.body().createTime,
                NoteParamConstant.UPDATE_TIME to resp.body().updateTime
            )
        }
    }

    private suspend fun pageInfo(notePageDto: NotePageDto): PageInfo<JsonObject> {
        val resp: Message<PageList<NoteRecord>> =
            eventBus.request<PageList<NoteRecord>>(NOTE_PAGE_INFO, notePageDto)
                .coAwait()
        return PageInfo(resp.body().map {
            json {
                obj(
                    NoteParamConstant.NOTE_ID to it.noteId,
                    NoteParamConstant.TITLE to it.title,
                    NoteParamConstant.CONTENT to it.content,
                    NoteParamConstant.OTHER_PROPERTIES to it.otherProperties,
                    NoteParamConstant.CREATE_TIME to it.createTime,
                    NoteParamConstant.UPDATE_TIME to it.updateTime
                )
            }
        }, resp.body().total)

    }
}

/**
 * 笔记相关的服务注册
 */
fun Vertx.deployNoteVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(NoteHandler(applicationContext))
    deployVerticle(NoteRepositoryVerticle(applicationContext))
}
