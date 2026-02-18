package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.value.VoteType
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TimelineRepository {
    // タイムラインの監視 (Flow)
    fun getTimelineStream(): Flow<List<TimelinePost>>

    // 投稿
    suspend fun createPost(
        userId: String,
        questId: String,
        mediaFile: File,
        mediaType: String,
        comment: String
    ): Result<Unit>

    // 投票
    suspend fun votePost(
        postId: String,
        userId: String,
        vote: VoteType
    ): Result<Unit>
}