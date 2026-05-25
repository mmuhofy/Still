package com.still.app.domain.repository

import com.still.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

// Pure Kotlin interface — implemented in data layer, consumed by use cases
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteById(id: Long): Flow<Note?>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun insert(note: Note): Long
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
    suspend fun deleteById(id: Long)
    suspend fun setPinned(id: Long, isPinned: Boolean)
}