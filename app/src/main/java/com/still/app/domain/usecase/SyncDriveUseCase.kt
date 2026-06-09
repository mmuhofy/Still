package com.still.app.domain.usecase

import com.still.app.domain.model.Note
import com.still.app.domain.repository.DriveRepository
import com.still.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Serializes all notes to JSON and uploads to Drive.
// Returns Result<Unit> — caller decides how to surface errors.
// UNTESTED — verify before use
class SyncDriveUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val driveRepository: DriveRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        val notes = noteRepository.getAllNotes().first()
        val json = Json.encodeToString(notes)
        return driveRepository.uploadBackup(json)
    }
}