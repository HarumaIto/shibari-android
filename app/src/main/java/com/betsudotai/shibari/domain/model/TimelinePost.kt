package com.betsudotai.shibari.domain.model

import com.betsudotai.shibari.domain.model.timeline.AuthorSnapshot
import com.betsudotai.shibari.domain.model.timeline.QuestSnapshot
import com.betsudotai.shibari.domain.value.MediaType
import com.betsudotai.shibari.domain.value.PostStatus
import com.betsudotai.shibari.domain.value.VoteType
import java.time.LocalDate

data class TimelinePost(
    val id: String,
    val userId: String,
    val questId: String,
    val author: AuthorSnapshot,
    val quest: QuestSnapshot,
    val mediaUrl: String?,
    val mediaType: MediaType?,
    val comment: String?,
    val status: PostStatus,
    val approvalCount: Int,
    val votes: Map<String, VoteType>, // user_uid to vote type
    val createdAt: LocalDate?
)