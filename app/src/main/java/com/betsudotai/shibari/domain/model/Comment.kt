package com.betsudotai.shibari.domain.model

import com.betsudotai.shibari.domain.model.timeline.AuthorSnapshot
import java.time.LocalDateTime

data class Comment (
    val id: String,
    val userId: String,
    val author: AuthorSnapshot,
    val text: String,
    val createdAt: LocalDateTime
)