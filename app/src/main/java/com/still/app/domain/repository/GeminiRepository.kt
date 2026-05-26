package com.still.app.domain.repository

interface GeminiRepository {
    /**
     * Returns a single inline completion suggestion for the given text context,
     * or null if the model returns nothing useful.
     */
    suspend fun getCompletion(context: String): Result<String?>

    /**
     * Returns up to [count] completion variants for long-press alternative picker.
     */
    suspend fun getCompletionVariants(context: String, count: Int): Result<List<String>>
}