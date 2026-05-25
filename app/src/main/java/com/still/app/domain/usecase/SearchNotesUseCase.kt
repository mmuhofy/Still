package com.still.app.domain.usecase

import com.still.app.domain.model.Note
import com.still.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    operator fun invoke(query: String): Flow<List<Note>> =
        repository.searchNotes(query.trim())
}