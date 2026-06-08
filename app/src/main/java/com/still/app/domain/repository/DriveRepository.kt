package com.still.app.domain.repository

// Pure Kotlin interface — no Android imports
interface DriveRepository {

    // Sign-in state
    suspend fun signIn(): Result<String>   // returns account email on success
    suspend fun signOut()
    fun getSignedInEmail(): String?        // null = not signed in

    // Sync
    suspend fun uploadBackup(json: String): Result<Unit>
    suspend fun downloadBackup(): Result<String?>  // null = no backup found on Drive

    // Last sync timestamp (epoch ms), 0 if never synced
    fun getLastSyncMs(): Long
}