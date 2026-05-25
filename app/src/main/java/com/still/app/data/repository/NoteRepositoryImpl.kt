package com.still.app.data.repository

import com.still.app.data.local.dao.NoteDao
import com.still.app.data.local.entity.toDomain
import com.still.app.data.local.entity.toEntity
import com.still.app.domain.model.Note
import com.still.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        dao.getAllNotes().map { list -> list.map { it.toDomain() } }

    override fun getNoteById(id: Long): Flow<Note?> =
        dao.getNoteById(id).map { it?.toDomain() }

    override fun searchNotes(query: String): Flow<List<Note>> =
        dao.searchNotes(query).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(note: Note): Long =
        dao.insert(note.toEntity())

    override suspend fun update(note: Note) =
        dao.update(note.toEntity())

    override suspend fun delete(note: Note) =
        dao.delete(note.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    override suspend fun setPinned(id: Long, isPinned: Boolean) =
        dao.setPinned(id, isPinned)
}