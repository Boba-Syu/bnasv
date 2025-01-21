package cn.bobasyu.catalog

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.base.PageList
import cn.bobasyu.base.pageListOf
import cn.bobasyu.constant.CatalogConsumerConstant
import cn.bobasyu.utils.generateId
import org.ktorm.dsl.between
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.count
import org.ktorm.entity.filter

class CatalogRepositoryVerticle(
    applicationContext: ApplicationContext
) : BaseRepositoryVerticle(applicationContext) {

    private val databaseHandler = applicationContext.databaseHandler

    override fun registerConsumer() = with(vertx.eventBus()) {
        asyncConsumer(CatalogConsumerConstant.CATALOG_PAGE_INFO, ::pageInfo)
        asyncConsumer(CatalogConsumerConstant.CATALOG_SAVE, ::save)
    }

    private fun pageInfo(catalogPageDto: CatalogPageDto): PageList<CatalogRecord> {
        val offset = (catalogPageDto.pageVal.pageNum - 1) * catalogPageDto.pageVal.pageNum
        val catalogRecordList: List<CatalogRecord> = databaseHandler.from(CatalogRecords).select().apply {
            limit(offset, offset + catalogPageDto.pageVal.pageSize)
            if (catalogPageDto.catalogId != null) {
                where { CatalogRecords.catalogId eq catalogPageDto.catalogId }
            }
            if (catalogPageDto.catalogName != null) {
                where { CatalogRecords.catalogName like catalogPageDto.catalogName }
            }
            if (catalogPageDto.bangumiSubjectId != null) {
                where { CatalogRecords.bangumiSubjectId eq catalogPageDto.bangumiSubjectId }
            }
            if (catalogPageDto.createTimeBegin != null && catalogPageDto.createTimeEnd != null) {
                where { CatalogRecords.createTime between catalogPageDto.createTimeBegin..catalogPageDto.createTimeEnd }
            }
        }.map { CatalogRecords.createEntity(it) }
        val total = count(catalogPageDto)
        return pageListOf(total, catalogRecordList)
    }

    private fun count(catalogPageDto: CatalogPageDto): Int {
        return databaseHandler.catalogRecords.apply {
            if (catalogPageDto.catalogId != null) {
                filter { CatalogRecords.catalogId eq catalogPageDto.catalogId }
            }
            if (catalogPageDto.catalogName != null) {
                filter { CatalogRecords.catalogName like catalogPageDto.catalogName }
            }
            if (catalogPageDto.bangumiSubjectId != null) {
                filter { CatalogRecords.bangumiSubjectId eq catalogPageDto.bangumiSubjectId }
            }
            if (catalogPageDto.createTimeBegin != null && catalogPageDto.createTimeEnd != null) {
                filter { CatalogRecords.createTime between catalogPageDto.createTimeBegin..catalogPageDto.createTimeEnd }
            }
        }.count()
    }

    private fun save(catalogSaveDto: CatalogSaveDto): String {
        databaseHandler.insertOrUpdate(CatalogRecords) {
            if (catalogSaveDto.catalogId != null) {
                set(it.catalogId, catalogSaveDto.catalogId)
            } else {
                set(it.catalogId, generateId())
            }
            if (catalogSaveDto.catalogName != null) {
                set(it.catalogName, catalogSaveDto.catalogName)
            }
            if (catalogSaveDto.bangumiSubjectId != null) {
                set(it.bangumiSubjectId, catalogSaveDto.bangumiSubjectId)
            }
            set(it.updateTime, catalogSaveDto.updateTime)
        }
        return SUCCESS
    }
}