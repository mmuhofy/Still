package com.still.app.data.repository

import com.still.app.BuildConfig
import com.still.app.domain.repository.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val GEMINI_MODEL = "gemini-2.0-flash-lite"
private const val GEMINI_BASE_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"
private const val CONNECT_TIMEOUT_MS = 5_000
private const val READ_TIMEOUT_MS = 10_000

class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    override suspend fun getCompletion(context: String): Result<String?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = callGemini(buildSinglePrompt(context), candidateCount = 1)
                extractFirstCandidate(raw)
            }
        }

    override suspend fun getCompletionVariants(
        context: String,
        count: Int,
    ): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = callGemini(buildVariantsPrompt(context), candidateCount = count)
                extractAllCandidates(raw)
            }
        }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private fun callGemini(prompt: String, candidateCount: Int): String {
        val url = URL("$GEMINI_BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
        val body = buildRequestBody(prompt, candidateCount)

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
        }

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val responseCode = conn.responseCode
        val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream.bufferedReader().readText()
        } else {
            val error = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
            throw Exception("Gemini API error $responseCode: $error")
        }
        conn.disconnect()
        return responseBody
    }

    // ── JSON builders ─────────────────────────────────────────────────────────

    private fun buildRequestBody(prompt: String, candidateCount: Int): String {
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                if (candidateCount > 1) put("candidateCount", candidateCount)
                put("maxOutputTokens", 60)
                put("temperature", 0.7)
                put("stopSequences", JSONArray().apply { put("\n") })
            })
        }.toString()
    }

    private fun buildSinglePrompt(context: String): String =
        """You are an inline writing assistant. Continue the text below with a short, natural completion (max 10 words). Return ONLY the continuation — no quotes, no explanation, no newlines.

Text: $context"""

    private fun buildVariantsPrompt(context: String): String =
        """You are an inline writing assistant. Provide 3 different short continuations (max 10 words each) for the text below. Return each on its own line, numbered 1. 2. 3. — no other text.

Text: $context"""

    // ── JSON parsers ──────────────────────────────────────────────────────────

    private fun extractFirstCandidate(json: String): String? {
        return try {
            val text = JSONObject(json)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
            text.ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractAllCandidates(json: String): List<String> {
        return try {
            val candidates = JSONObject(json).getJSONArray("candidates")
            val results = mutableListOf<String>()
            for (i in 0 until candidates.length()) {
                val text = candidates.getJSONObject(i)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                // Strip leading "1. " / "2. " numbering if present
                val cleaned = text.replace(Regex("""^\d+\.\s*"""), "").trim()
                if (cleaned.isNotBlank()) results += cleaned
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }
}