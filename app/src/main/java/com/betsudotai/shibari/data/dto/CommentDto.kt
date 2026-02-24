package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.Comment
import com.betsudotai.shibari.domain.model.timeline.AuthorSnapshot
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class CommentDto (
    @DocumentId val documentId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("author") val author: Map<String, String?> = emptyMap(),
    @PropertyName("text") val text: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp? = null,
) {
    fun toDomain(): Comment {
        return Comment(
            id = documentId,
            userId = userId,
            author = AuthorSnapshot(
                displayName = author["displayName"] ?: "Unknown",
                photoUrl = author["photoUrl"]
            ),
            text = text,
            createdAt = createdAt?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDateTime()
                ?: java.time.LocalDateTime.now()
        )
    }
}