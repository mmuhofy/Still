package com.still.app.domain.usecase

import com.still.app.domain.repository.NoteRepository
import javax.inject.Inject

class PinNoteUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(id: Long, isPinned: Boolean) =
        repository.setPinned(id, isPinned)
}