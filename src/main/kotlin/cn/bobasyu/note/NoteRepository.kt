package cn.bobasyu.note

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlClient
import cn.bobasyu.entity.NoteDto
import cn.bobasyu.entity.NoteRecord
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import io.vertx.core.eventbus.Message

class NoteRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractNoteRepository(applicationContext) {

    override suspend fun handleQueryByIdEvent(message: Message<Int>) = handleEvent(message) {
        queryNoteById(message.body())
    }

    override suspend fun handleUpdateEvent(message: Message<NoteDto>) = handleEvent(message) {
        save(message.body())
    }

    private fun queryNoteById(id: Int): NoteRecord {
        val noteRecord: NoteRecord? = SqlClient.withSession { session ->
            session.find(NoteRecord::class.java, id)
        }.await().indefinitely()
        if (noteRecord == null) {
            throw NoSuchRecordInDatabaseException("id: $id")
        }
        return noteRecord
    }

    private fun save(noteDto: NoteDto)  {
        val noteRecord: NoteRecord = noteDto.toJson().parseJson(NoteRecord::class.java)
        SqlClient.withSession { session ->
            session.persist(noteRecord)
        }.await().indefinitely()
    }
}