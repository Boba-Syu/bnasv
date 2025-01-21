package cn.bobasyu.catalog

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseServiceVerticle
import cn.bobasyu.base.PageInfo
import cn.bobasyu.base.PageList
import cn.bobasyu.constant.CatalogConsumerConstant.CATALOG_PAGE_INFO
import cn.bobasyu.note.NoteParamConstant
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.coAwait

class CatalogHandler(
    private val applicationContext: ApplicationContext
) : BaseServiceVerticle(applicationContext) {

    private val eventBus: EventBus by lazy { vertx.eventBus() }

    override fun setUserRouter() = with(applicationContext.router) {
        val baseUrl = "/catalog"
        get("${baseUrl}/pageInfo").doHandler(::pageInfo)
    }

    suspend fun pageInfo(catalogPageDto: CatalogPageDto): PageInfo<JsonObject> {
        val message = eventBus.request<PageList<CatalogRecord>>(CATALOG_PAGE_INFO, catalogPageDto).coAwait()
        return PageInfo(message.body().map {
            json {
                obj(
                    CatalogParamConstant.CATALOG_ID to it.catalogId,
                    CatalogParamConstant.CATALOG_NAME to it.catalogName,
                    CatalogParamConstant.BANGUMI_SUBJECT_ID to it.bangumiSubjectId,
                    CatalogParamConstant.CREATE_TIME to it.createTime,
                    CatalogParamConstant.UPDATE_TIME to it.updateTime,
                    NoteParamConstant.NOTE_ID to it.otherProperties.noteId
                )
            }
        }, message.body().total)
    }

}

fun Vertx.deployCatalogVerticle(applicationContext: ApplicationContext): Vertx = this.apply {
    deployVerticle(CatalogHandler(applicationContext))
    deployVerticle(CatalogRepositoryVerticle(applicationContext))
}