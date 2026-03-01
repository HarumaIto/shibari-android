package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.GroupRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.TimelineRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.UserRemoteDataSource
import com.betsudotai.shibari.data.dto.CommentDto
import com.betsudotai.shibari.data.dto.TimelinePostDto
import com.betsudotai.shibari.domain.model.Comment
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.value.VoteType
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject

class TimelineRepositoryImpl @Inject constructor(
    private val timelineDataSource: TimelineRemoteDataSource,
    private val questDataSource: QuestRemoteDataSource,
    private val userDataSource: UserRemoteDataSource,
    private val groupDataSource: GroupRemoteDataSource
) : TimelineRepository {

    override fun getTimelineStream(groupId: String): Flow<List<TimelinePost>> {
        // DTOのFlowをDomainモデルのFlowに変換して流す
        return timelineDataSource.getTimelineStream(groupId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createPost(
        userId: String,
        questId: String,
        groupId: String,
        mediaFile: File,
        mediaType: String,
        comment: String
    ): Result<Unit> {
        return runCatching {
            // 1. 画像/動画をアップロード
            val path = "posts/${UUID.randomUUID()}"
            val downloadUrl = timelineDataSource.uploadMedia(mediaFile, path)

            // 2. クエスト情報のスナップショットを取得
            // (QuestRemoteDataSourceを再利用！)
            val questDto = questDataSource.fetchQuest(questId)
                ?: throw IllegalStateException("Quest not found")

            // 3. ユーザー情報のスナップショットを取得
            val userDto = userDataSource.getUser(userId)
                ?: throw IllegalStateException("User not found")
            val authorMap = mapOf(
                "displayName" to userDto.displayName,
                "photoUrl" to userDto.photoUrl
            )

            // 4. DTOを作成
            val newPost = TimelinePostDto(
                documentId = UUID.randomUUID().toString(),
                userId = userId,
                questId = questId,
                groupId = groupId, // Include groupId here
                author = authorMap,
                quest = mapOf(
                    "title" to questDto.title,
                    "type" to questDto.type
                ),
                mediaUrl = downloadUrl,
                mediaType = mediaType,
                comment = comment,
                createdAt = Timestamp.now()
            )

            // 5. Firestoreに保存
            timelineDataSource.createPost(newPost)
        }
    }

    override suspend fun votePost(postId: String, userId: String, vote: VoteType): Result<Unit> {
        return runCatching {
            val userDto = userDataSource.getUser(userId)
            if (userDto == null || userDto.groupId == null) {
                throw IllegalStateException("User not found")
            }
            val groupDto = groupDataSource.getGroupDetails(userDto.groupId)
                ?: throw IllegalStateException("Group not found")
            timelineDataSource.updateVote(postId, userId, vote.name, groupDto.memberIds.size)
        }
    }

    override fun getCommentsStream(postId: String): Flow<List<Comment>> {
        return timelineDataSource.getCommentsStream(postId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addComment(postId: String, userId: String, text: String): Result<Unit> {
        return runCatching {
            // 現在のユーザー情報を取得してスナップショットを作る
            val userDto = userDataSource.getUser(userId)
                ?: throw IllegalStateException("User not found")

            val authorMap = mapOf(
                "displayName" to userDto.displayName,
                "photoUrl" to userDto.photoUrl
            )

            val newComment = CommentDto(
                documentId = UUID.randomUUID().toString(),
                userId = userId,
                author = authorMap,
                text = text,
                createdAt = Timestamp.now()
            )

            timelineDataSource.addComment(postId, newComment)
        }
    }

    override suspend fun getMyPostsForQuests(userId: String, questIds: List<String>): List<TimelinePost> {
        return timelineDataSource.getMyPostsForQuests(userId, questIds).map { it.toDomain() }
    }
}