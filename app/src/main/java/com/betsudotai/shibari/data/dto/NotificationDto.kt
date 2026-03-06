package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.AppNotification
import com.betsudotai.shibari.domain.value.NotificationType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.ZoneId

data class NotificationDto(
    @DocumentId val documentId: String = "",
    @PropertyName("type") val type: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("body") val body: String = "",
    @PropertyName("senderId") val senderId: String? = null,
    @PropertyName("targetId") val targetId: String? = null,
    @PropertyName("isRead") val isRead: Boolean = false,
    @PropertyName("createdAt") val createdAt: Timestamp? = null
) {
    fun toDomain(): AppNotification {
        return AppNotification(
            id = documentId,
            type = try { NotificationType.valueOf(type) } catch (e: Exception) {
                e.printStackTrace()
                NotificationType.QUEST_POSTED
            },
            title = title,
            body = body,
            senderId = senderId,
            targetId = targetId,
            isRead = isRead,
            createdAt = createdAt?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        )
    }
}
