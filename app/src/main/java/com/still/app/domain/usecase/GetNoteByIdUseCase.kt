package com.still.app.domain.usecase

import com.still.app.domain.model.Note
import com.still.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    operator fun invoke(id: Long): Flow<Note?> = repository.getNoteById(id)
}