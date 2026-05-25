package com.still.app.domain.usecase

import com.still.app.domain.model.Note
import com.still.app.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(note: Note) {
        repository.update(note.copy(updatedAt = System.currentTimeMillis()))
    }
}