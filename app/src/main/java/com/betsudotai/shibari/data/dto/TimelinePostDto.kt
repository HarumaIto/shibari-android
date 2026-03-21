package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.value.PostStatus
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.model.timeline.AiJudgment
import com.betsudotai.shibari.domain.value.JudgmentResult
import com.betsudotai.shibari.domain.model.timeline.AuthorSnapshot
import com.betsudotai.shibari.domain.model.timeline.QuestSnapshot
import com.betsudotai.shibari.domain.value.VoteType
import com.betsudotai.shibari.domain.value.MediaType
import com.betsudotai.shibari.domain.value.QuestType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.LocalDateTime
import java.time.ZoneId

data class TimelinePostDto(
    @DocumentId val documentId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("questId") val questId: String = "",
    @PropertyName("groupId") val groupId: String = "",

    @PropertyName("author") val author: Map<String, String?> = emptyMap(),
    @PropertyName("quest") val quest: Map<String, String> = emptyMap(),

    @PropertyName("mediaUrl") val mediaUrl: String = "",
    @PropertyName("mediaType") val mediaType: String = "image",
    @PropertyName("comment") val comment: String = "",

    @PropertyName("status") val status: String = "pending",
    @PropertyName("approvalCount") val approvalCount: Int = 0,
    @PropertyName("rejectCount") val rejectCount: Int? = 0,

    @PropertyName("votes") val votes: Map<String, String> = emptyMap(),

    @PropertyName("createdAt") val createdAt: Timestamp? = null,

    @PropertyName("commentCount") val commentCount: Int? = 0,
    @PropertyName("latestComments") val latestComments: List<String>? = emptyList(),

    @PropertyName("aiJudgment") val aiJudgment: Map<String, Any?>? = null
) {
    fun toDomain(): TimelinePost {
        return TimelinePost(
            id = documentId,
            userId = userId,
            questId = questId,
            groupId = groupId,
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
            rejectCount = rejectCount ?: 0,
            votes = votes.mapValues {
                try { VoteType.valueOf(it.value.uppercase()) } catch (e: Exception) { VoteType.APPROVE }
            },
            createdAt = createdAt?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                ?: LocalDateTime.now(),
            commentCount = commentCount ?: 0,
            latestComments = latestComments ?: emptyList(),
            aiJudgment = aiJudgment?.let { aiJudgmentMap ->
                val judgedAtTimestamp = aiJudgmentMap["judgedAt"] as? Timestamp
                val reasonText = aiJudgmentMap["reason"] as? String
                val resultString = aiJudgmentMap["result"] as? String

                if (judgedAtTimestamp != null && reasonText != null && resultString != null) {
                    AiJudgment(
                        judgedAt = judgedAtTimestamp.toDate(),
                        reason = reasonText,
                        result = try {
                            JudgmentResult.valueOf(resultString.uppercase())
                        } catch (e: Exception) {
                            JudgmentResult.UNKNOWN
                        }
                    )
                } else {
                    null
                }
            }
        )
    }
}