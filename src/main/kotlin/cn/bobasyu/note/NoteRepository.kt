package cn.bobasyu.note

import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.base.NoSuchRecordInDatabaseException
import cn.bobasyu.databeses.MySqlClient
import cn.bobasyu.databeses.SqlGenerator
import cn.bobasyu.entity.NoteDto
import cn.bobasyu.entity.NoteRecord
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class NoteRepositoryVerticle(
    applicationContext: ApplicationContext
) : AbstractNoteRepository(applicationContext) {

    val mySqlClient: MySqlClient = applicationContext.mySqlClient

    override suspend fun handleQueryByIdEvent(message: Message<Int>) {
        queryNoteById(message.body())
            .onSuccess { message.reply(it as NoteRecord) }
            .onFailure { message.fail(500, it.message) }
    }

    override suspend fun handleUpdateEvent(message: Message<NoteDto>) {
        save(message.body())

            .onSuccess { message.reply("success") }
            .onFailure { message.fail(500, it.message) }
    }

    private fun queryNoteById(id: Int): Future<NoteRecord> {
        return SqlGenerator(NoteRecord::class)
            .select().where()
            .eq(NoteRecord::noteId, id)
            .execute(mySqlClient)
            .map {
                if ((it as List<*>).isEmpty()) {
                    throw NoSuchRecordInDatabaseException("id: $id")
                }
                it.first() as NoteRecord
            }
    }

    private fun save(noteDto: NoteDto): Future<Unit> = when {
        noteDto.noteId == null -> insert(noteDto)
        else -> update(noteDto)
    }

    private fun update(noteDto: NoteDto): Future<Unit> {
        val list: List<KProperty<NoteRecord>> = ArrayList()
        NoteDto::class.memberProperties
        return SqlGenerator(NoteRecord::class)
            .update(list)
            .where()
            .eq(NoteRecord::noteId, noteDto.noteId!!)
            .execute(mySqlClient)
            .map {}
    }

    private fun insert(noteDto: NoteDto): Future<Unit> {
        val list: List<KProperty<NoteRecord>> = ArrayList()
        return SqlGenerator(NoteRecord::class)
            .insert()
            .values(noteDto)
            .execute(mySqlClient)
            .map {}

    }
}