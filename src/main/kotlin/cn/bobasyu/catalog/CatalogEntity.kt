package cn.bobasyu.catalog

import cn.bobasyu.base.PageVal
import cn.bobasyu.constant.BaseRecordConstant.CREATE_TIME_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.OTHER_PROPERTIES_COLUMN
import cn.bobasyu.constant.BaseRecordConstant.UPDATE_TIME_COLUMN
import cn.bobasyu.constant.CatalogRecordConstant.BANGUMI_SUBJECT_ID_COLUMN
import cn.bobasyu.constant.CatalogRecordConstant.CATALOG_ID_COLUMN
import cn.bobasyu.constant.CatalogRecordConstant.CATALOG_NAME_COLUMN
import cn.bobasyu.constant.CatalogRecordConstant.CATALOG_RECORD
import cn.bobasyu.databeses.DatabaseHandler
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.json
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.io.Serializable
import java.time.LocalDateTime

interface CatalogRecord : Entity<CatalogRecord>, Serializable {
    companion object : Entity.Factory<CatalogRecord>()

    var catalogId: Long
    var catalogName: String
    var bangumiSubjectId: Long
    var otherProperties: OtherProperties
    var createTime: LocalDateTime
    var updateTime: LocalDateTime

    data class OtherProperties(
        val noteId: Long?,
    ): Serializable
}

object CatalogRecords : Table<CatalogRecord>(CATALOG_RECORD) {
    val catalogId: Column<Long> = long(CATALOG_ID_COLUMN).primaryKey().bindTo { it.catalogId }
    val catalogName: Column<String> = varchar(CATALOG_NAME_COLUMN).bindTo { it.catalogName }
    val bangumiSubjectId: Column<Long> = long(BANGUMI_SUBJECT_ID_COLUMN).bindTo { it.bangumiSubjectId }
    val otherProperties: Column<CatalogRecord.OtherProperties> =
        json<CatalogRecord.OtherProperties>(OTHER_PROPERTIES_COLUMN).bindTo { it.otherProperties }
    val createTime: Column<LocalDateTime> = datetime(CREATE_TIME_COLUMN).bindTo { it.createTime }
    val updateTime: Column<LocalDateTime> = datetime(UPDATE_TIME_COLUMN).bindTo { it.updateTime }
}

val DatabaseHandler.catalogRecords get() = this.database.sequenceOf(CatalogRecords)

data class CatalogPageDto(
    val pageVal: PageVal,
    val catalogId: Long?,
    val catalogName: String?,
    val bangumiSubjectId: Long?,
    val createTimeBegin: LocalDateTime?,
    val createTimeEnd: LocalDateTime?
) : Serializable

data class CatalogSaveDto(
    var catalogId: Long?,
    var catalogName: String?,
    var bangumiSubjectId: Long?,
    var otherProperties: Map<String, Any> = mapOf(),
    var updateTime: LocalDateTime = LocalDateTime.now()
)