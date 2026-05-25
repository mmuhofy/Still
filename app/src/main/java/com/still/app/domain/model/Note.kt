package com.still.app.domain.model

// Pure Kotlin domain model — zero Android/Room imports
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
)