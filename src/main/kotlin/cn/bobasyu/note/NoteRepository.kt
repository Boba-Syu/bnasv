package cn.bobasyu.note

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.entity.NoteDto
import cn.bobasyu.entity.NoteRecord
import cn.bobasyu.entity.NoteRecords
import cn.bobasyu.entity.noteRecords
import cn.bobasyu.utils.generateId
import io.vertx.core.eventbus.Message
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.time.LocalDateTime

class NoteRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractNoteRepository(applicationContext) {

    private val databaseHandler = applicationContext.databaseHandler

    override suspend fun handleQueryByIdEvent(message: Message<Long>) = handleEvent(message) {
        queryNoteById(message.body())
    }

    override suspend fun handleUpdateEvent(message: Message<NoteDto>) = handleEvent(message) {
        save(message.body())
        SUCCESS
    }

    private fun queryNoteById(id: Long): NoteRecord {
        val noteRecord: NoteRecord? = databaseHandler.noteRecords.find { it.noteId eq id }
        if (noteRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return noteRecord
    }

    private fun save(noteDto: NoteDto) = when {
        noteDto.noteId == null -> insert(noteDto)
        else -> update(noteDto)
    }

    private fun update(noteDto: NoteDto) {
        databaseHandler.update(NoteRecords) {
            if (noteDto.title != null) {
                set(it.title, noteDto.title)
            }
            if (noteDto.content != null) {
                set(it.content, noteDto.content)
            }
            set(it.updateTime, noteDto.updateTime)
            where { it.noteId eq noteDto.noteId!! }
        }
    }

    private fun insert(noteDto: NoteDto) {
        val noteRecord = NoteRecord {
            noteId = generateId()
            if (noteDto.title != null) {
                title = noteDto.title
            }
            if (noteDto.content != null) {
                title = noteDto.content
            }
            createTime = LocalDateTime.now()
            updateTime = LocalDateTime.now()
        }
        databaseHandler.noteRecords.add(noteRecord)
    }
}