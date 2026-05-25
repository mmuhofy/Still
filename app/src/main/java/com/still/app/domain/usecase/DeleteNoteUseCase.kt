package com.still.app.domain.usecase

import com.still.app.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteById(id)
}