package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.CommentDto
import com.betsudotai.shibari.data.dto.TimelinePostDto
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TimelineRemoteDataSource {
    fun getTimelineStream(): Flow<List<TimelinePostDto>>
    suspend fun uploadMedia(file: File, path: String): String
    suspend fun createPost(postDto: TimelinePostDto)
    suspend fun updateVote(postId: String, userId: String, vote: String)
    fun getCommentsStream(postId: String): Flow<List<CommentDto>>
    suspend fun addComment(postId: String, commentDto: CommentDto)
}