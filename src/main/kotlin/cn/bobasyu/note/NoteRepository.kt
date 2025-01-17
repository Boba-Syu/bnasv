package cn.bobasyu.note

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.BaseRepositoryVerticle
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.note.NoteRepositoryConsumerConstant.NOTE_PAGE_INFO
import cn.bobasyu.note.NoteRepositoryConsumerConstant.NOTE_QUERY_BY_ID_EVENT
import cn.bobasyu.note.NoteRepositoryConsumerConstant.NOTE_UPDATE_EVENT
import cn.bobasyu.utils.generateId
import org.ktorm.dsl.*
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.find

class NoteRepositoryVerticle(
    applicationContext: ApplicationContext
) : BaseRepositoryVerticle(applicationContext) {
    private val databaseHandler = applicationContext.databaseHandler

    override fun registerConsumer() = with(vertx.eventBus()) {
        asyncConsumer(NOTE_QUERY_BY_ID_EVENT, ::queryNoteById)
        asyncConsumer(NOTE_UPDATE_EVENT, ::save)
        asyncConsumer(NOTE_PAGE_INFO, ::pageInfo)
    }

    private fun count(notePageDto: NotePageDto): Int {
        return databaseHandler.noteRecords.apply {
            if (notePageDto.noteId != null) {
                filter { NoteRecords.noteId eq notePageDto.noteId }
            }
            if (notePageDto.title != null) {
                filter { NoteRecords.title eq notePageDto.title }
            }
            if (notePageDto.content != null) {
                filter { NoteRecords.content eq notePageDto.content }
            }
            if (notePageDto.createTimeBegin != null && notePageDto.createTimeEnd != null) {
                filter { NoteRecords.createTime between notePageDto.createTimeBegin..notePageDto.createTimeEnd }
            }
            if (notePageDto.updateTimeBegin != null && notePageDto.updateTimeEnd != null) {
                filter { NoteRecords.updateTime between notePageDto.updateTimeBegin..notePageDto.updateTimeEnd }
            }
        }.count()
    }

    private fun pageInfo(notePageDto: NotePageDto): List<NoteRecord> {
        val offset = (notePageDto.pageVal.pageNum - 1) * notePageDto.pageVal.pageSize
        return databaseHandler.from(NoteRecords).select().apply {
            limit(offset, offset + notePageDto.pageVal.pageSize)
            if (notePageDto.noteId != null) {
                where { NoteRecords.noteId eq notePageDto.noteId }
            }
            if (notePageDto.title != null) {
                where { NoteRecords.title eq notePageDto.title }
            }
            if (notePageDto.content != null) {
                where { NoteRecords.content eq notePageDto.content }
            }
            if (notePageDto.createTimeBegin != null && notePageDto.createTimeEnd != null) {
                where { NoteRecords.createTime between notePageDto.createTimeBegin..notePageDto.createTimeEnd }
            }
            if (notePageDto.updateTimeBegin != null && notePageDto.updateTimeEnd != null) {
                where { NoteRecords.updateTime between notePageDto.updateTimeBegin..notePageDto.updateTimeEnd }
            }
        }.map { row -> NoteRecords.createEntity(row) }
    }


    private fun queryNoteById(id: Long): NoteRecord {
        val noteRecord: NoteRecord? = databaseHandler.noteRecords.find { it.noteId eq id }
        if (noteRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return noteRecord
    }

    private fun save(noteDto: NoteDto) {
        databaseHandler.insertOrUpdate(NoteRecords) {
            if (noteDto.noteId != null) {
                set(it.noteId, noteDto.noteId)
            } else {
                set(it.noteId, generateId())
            }
            if (noteDto.title != null) {
                set(it.title, noteDto.title)
            }
            if (noteDto.content != null) {
                set(it.content, noteDto.content)
            }
            if (noteDto.otherProperties != null) {
                set(it.otherProperties, noteDto.otherProperties)
            }
            set(it.updateTime, noteDto.updateTime)
        }
    }
}