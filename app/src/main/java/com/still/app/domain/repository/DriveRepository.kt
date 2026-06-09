package com.still.app.domain.repository

// Pure Kotlin interface — no Android imports
interface DriveRepository {

    // Sign-in state
    suspend fun saveSignedInEmail(email: String)
    suspend fun signOut()
    fun getSignedInEmail(): String?

    // Sync
    suspend fun uploadBackup(json: String): Result<Unit>
    suspend fun downloadBackup(): Result<String?>

    // Last sync timestamp (epoch ms), 0 if never synced
    fun getLastSyncMs(): Long
}