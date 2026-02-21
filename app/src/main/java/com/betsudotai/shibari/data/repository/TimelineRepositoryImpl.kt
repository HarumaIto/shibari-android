package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.TimelineRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.UserRemoteDataSource
import com.betsudotai.shibari.data.dto.TimelinePostDto
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
    private val userDataSource: UserRemoteDataSource
) : TimelineRepository {

    override fun getTimelineStream(): Flow<List<TimelinePost>> {
        // DTOのFlowをDomainモデルのFlowに変換して流す
        return timelineDataSource.getTimelineStream().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createPost(
        userId: String,
        questId: String,
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
                id = UUID.randomUUID().toString(),
                userId = userId,
                questId = questId,
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
            timelineDataSource.updateVote(postId, userId, vote.name)
        }
    }
}