package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.value.PostStatus
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.model.timeline.AuthorSnapshot
import com.betsudotai.shibari.domain.model.timeline.QuestSnapshot
import com.betsudotai.shibari.domain.value.VoteType
import com.betsudotai.shibari.domain.value.MediaType
import com.betsudotai.shibari.domain.value.QuestType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate
import java.time.ZoneId

data class TimelinePostDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("questId") val questId: String = "",
    @PropertyName("groupId") val groupId: String = "", // Add groupId here

    // スナップショット (Mapとして保存される)
    @PropertyName("author") val author: Map<String, String?> = emptyMap(),
    @PropertyName("quest") val quest: Map<String, String> = emptyMap(),

    @PropertyName("mediaUrl") val mediaUrl: String = "",
    @PropertyName("mediaType") val mediaType: String = "image",
    @PropertyName("comment") val comment: String = "",

    @PropertyName("status") val status: String = "pending",
    @PropertyName("approvalCount") val approvalCount: Int = 0,

    // 誰が投票したか: Map<UserId, VoteTypeString>
    @PropertyName("votes") val votes: Map<String, String> = emptyMap(),

    @PropertyName("createdAt") val createdAt: Timestamp? = null
) {
    fun toDomain(): TimelinePost {
        // 安全に変換するロジック
        return TimelinePost(
            id = id,
            userId = userId,
            questId = questId,
            groupId = groupId, // Map groupId here
            author = AuthorSnapshot(
                displayName = author["displayName"] ?: "Unknown",
                photoUrl = author["photoUrl"]
            ),
            quest = QuestSnapshot(
                title = quest["title"] ?: "Unknown",
                type = try { QuestType.valueOf(quest["type"] ?: "ROUTINE") } catch (e: Exception) { QuestType.ROUTINE }
            ),
            mediaUrl = mediaUrl,
            mediaType = try { MediaType.valueOf(mediaType.uppercase()) } catch (e: Exception) { MediaType.IMAGE },
            comment = comment,
            status = try { PostStatus.valueOf(status.uppercase()) } catch (e: Exception) { PostStatus.PENDING },
            approvalCount = approvalCount,
            votes = votes.mapValues {
                try { VoteType.valueOf(it.value.uppercase()) } catch (e: Exception) { VoteType.APPROVE }
            },
            // Timestamp -> Date
            createdAt = createdAt?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                ?: LocalDate.now()
        )
    }
}