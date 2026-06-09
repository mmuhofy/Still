package com.still.app.domain.model

import kotlinx.serialization.Serializable

// Pure Kotlin domain model — zero Android/Room imports
@Serializable
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
)