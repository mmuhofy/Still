package com.still.app.data.repository

import android.util.Log
import com.still.app.BuildConfig
import com.still.app.domain.repository.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val TAG = "GeminiRepo"
private const val CONNECT_TIMEOUT_MS = 5_000
private const val READ_TIMEOUT_MS = 10_000
private const val GEMINI_BASE =
    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent"

// Priority order — current working model is remembered across requests
private val MODEL_CHAIN = listOf(
    "gemini-3.5-flash",
    "gemini-2.5-flash",
    "gemini-3.1-flash-lite",
    "gemini-3-flash",
    "gemini-2.5-flash-lite",
)

class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    // Index of the currently active model — sticks until a 429 forces fallback
    @Volatile private var currentModelIndex = 0

    override suspend fun getCompletion(context: String): Result<String?> =
        withContext(Dispatchers.IO) {
            runCatching { callWithFallback(buildSinglePrompt(context), candidateCount = 1) { extractFirstCandidate(it) } }
        }

    override suspend fun getCompletionVariants(context: String, count: Int): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching { callWithFallback(buildVariantsPrompt(context), candidateCount = 1) { extractAllCandidates(it) } }
        }

    // ── Fallback logic ────────────────────────────────────────────────────────

    private fun <T> callWithFallback(
        prompt: String,
        candidateCount: Int,
        parse: (String) -> T,
    ): T {
        // Start from current working model, try each subsequent one on 429
        val startIndex = currentModelIndex
        for (offset in MODEL_CHAIN.indices) {
            val idx = (startIndex + offset) % MODEL_CHAIN.size
            val model = MODEL_CHAIN[idx]
            try {
                val raw = callGemini(model, prompt, candidateCount)
                // Success — lock to this model
                if (currentModelIndex != idx) {
                    Log.i(TAG, "Switched to model: $model")
                    currentModelIndex = idx
                }
                return parse(raw)
            } catch (e: Exception) {
                val is429 = e.message?.contains("429") == true
                if (is429) {
                    Log.w(TAG, "[$model] 429 rate limit — trying next model")
                    // continue to next model
                } else {
                    // Non-429 error — don't fallback, surface it
                    throw e
                }
            }
        }
        throw Exception("All models rate limited")
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private fun callGemini(model: String, prompt: String, candidateCount: Int): String {
        val url = URL(GEMINI_BASE.format(model) + "?key=${BuildConfig.GEMINI_API_KEY}")
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
            Log.e(TAG, "[$model] HTTP $responseCode: $error")
            throw Exception("HTTP $responseCode: $error")
        }
        conn.disconnect()

        Log.d(TAG, "[$model] response: $responseBody")
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
                put("maxOutputTokens", 80)
                put("temperature", 0.4)
            })
        }.toString()
    }

    private fun buildSinglePrompt(context: String): String =
        """You are an autocomplete engine built into a note-taking app. Your only job is to continue the user's text naturally.

Rules:
- Output ONLY the continuation text — no quotes, no explanation, no formatting
- Maximum 10 words
- Must flow naturally from the last word of the input
- Never start with a space (the app adds spacing if needed)
- Never repeat what was already written
- If the text ends mid-sentence, complete that sentence
- If the text ends with a complete sentence, start the next logical sentence
- Never output newlines

Input: $context
Continuation:"""

    private fun buildVariantsPrompt(context: String): String =
        """You are an autocomplete engine. Give 3 different short continuations for the text below.

Rules:
- Each continuation max 10 words
- Each on its own line, no numbering, no bullets
- Each must flow naturally from the input
- No quotes, no explanations

Input: $context
Continuations:"""

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
                .lines()
                .firstOrNull()
                ?.trim()
            text?.ifBlank { null }
        } catch (e: Exception) {
            Log.e(TAG, "extractFirstCandidate failed: ${e.message}\nJSON: $json")
            null
        }
    }

    private fun extractAllCandidates(json: String): List<String> {
        return try {
            val root = JSONObject(json)
            val candidates = root.getJSONArray("candidates")
            val results = mutableListOf<String>()

            if (candidates.length() > 1) {
                for (i in 0 until candidates.length()) {
                    val text = candidates.getJSONObject(i)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim()
                    if (text.isNotBlank()) results += text
                }
            } else {
                val raw = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                raw.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { results += it }
            }

            results.take(3)
        } catch (e: Exception) {
            Log.e(TAG, "extractAllCandidates failed: ${e.message}\nJSON: $json")
            emptyList()
        }
    }
}