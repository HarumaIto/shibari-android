package com.betsudotai.shibari.domain.model

import com.betsudotai.shibari.domain.model.timeline.AuthorSnapshot
import com.betsudotai.shibari.domain.model.timeline.QuestSnapshot

data class Timeline(
    val id: String,
    val userId: String,
    val questId: String,
    val author: AuthorSnapshot,
    val quest: QuestSnapshot,
    val mediaUrl: String?,
    val mediaType: String?, // image | video
    val comment: String?,
    val status: String, // pending | approved | rejected | disputed
    val approvalCount: Int,
    val votes: Map<String, String>, // user_uid to vote status (approve | reject)
    val createdAt: Long
)