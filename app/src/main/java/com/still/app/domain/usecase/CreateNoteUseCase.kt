package com.still.app.domain.usecase

import com.still.app.domain.model.Note
import com.still.app.domain.repository.NoteRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(title: String, content: String): Long {
        val now = System.currentTimeMillis()
        val note = Note(
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now,
        )
        return repository.insert(note)
    }
}