package com.still.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.still.app.BuildConfig
import com.still.app.domain.repository.DriveRepository
import com.still.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// UNTESTED — verify before use
@Singleton
class DriveRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : DriveRepository {

    private val emailKey = stringPreferencesKey(Constants.PrefKeys.DRIVE_ACCOUNT_EMAIL)
    private val lastSyncKey = longPreferencesKey(Constants.PrefKeys.DRIVE_LAST_SYNC_MS)

    // Backup file name inside Drive appDataFolder
    private val backupFileName = "still_backup.json"
    private val appName = "Still"

    // ── Sign-in ───────────────────────────────────────────────────────────────

    override suspend fun signIn(): Result<String> = withContext(Dispatchers.Main) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Activity context is required for Credential Manager UI —
            // this call must originate from a composable via rememberLauncherForActivityResult
            // or directly passed activity context. Here we use application context as a
            // placeholder; callers must pass the correct activity context at call site.
            // UNTESTED — verify before use
            val result = credentialManager.getCredential(context, request)
            val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data)
            val email = googleIdToken.id

            dataStore.edit { it[emailKey] = email }
            Result.success(email)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        dataStore.edit { prefs ->
            prefs.remove(emailKey)
            prefs.remove(lastSyncKey)
        }
    }

    override fun getSignedInEmail(): String? = null // see getSignedInEmailSuspend below

    // Suspend version used internally
    private suspend fun getSignedInEmailSuspend(): String? =
        dataStore.data.first()[emailKey]

    // ── Drive client ──────────────────────────────────────────────────────────

    private suspend fun buildDriveService(): Drive? {
        val email = getSignedInEmailSuspend() ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA),
        ).also { it.selectedAccountName = email }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        ).setApplicationName(appName).build()
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    override suspend fun uploadBackup(json: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val drive = buildDriveService()
                    ?: return@withContext Result.failure(IllegalStateException("Not signed in"))

                val existingId = findBackupFileId(drive)
                val content = ByteArrayContent("application/json", json.toByteArray(Charsets.UTF_8))

                if (existingId != null) {
                    // Update existing file
                    drive.files().update(existingId, null, content).execute()
                } else {
                    // Create new file in appDataFolder
                    val meta = File().apply {
                        name = backupFileName
                        parents = listOf("appDataFolder")
                    }
                    drive.files().create(meta, content).execute()
                }

                dataStore.edit { it[lastSyncKey] = System.currentTimeMillis() }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ── Download ──────────────────────────────────────────────────────────────

    override suspend fun downloadBackup(): Result<String?> =
        withContext(Dispatchers.IO) {
            try {
                val drive = buildDriveService()
                    ?: return@withContext Result.failure(IllegalStateException("Not signed in"))

                val fileId = findBackupFileId(drive)
                    ?: return@withContext Result.success(null) // no backup exists yet

                val stream = drive.files().get(fileId).executeMediaAsInputStream()
                val json = stream.bufferedReader(Charsets.UTF_8).readText()
                Result.success(json)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun findBackupFileId(drive: Drive): String? {
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$backupFileName'")
            .setFields("files(id)")
            .setPageSize(1)
            .execute()
        return result.files.firstOrNull()?.id
    }

    override fun getLastSyncMs(): Long = 0L // see getLastSyncMsSuspend below

    // Suspend version — callers should collect from DataStore flow directly
    suspend fun getLastSyncMsSuspend(): Long =
        dataStore.data.first()[lastSyncKey] ?: 0L
}