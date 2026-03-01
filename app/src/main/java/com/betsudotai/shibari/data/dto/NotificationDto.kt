package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.AppNotification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.ZoneId

data class NotificationDto(
    @DocumentId val documentId: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("body") val body: String = "",
    @PropertyName("isRead") val isRead: Boolean = false,
    @PropertyName("createdAt") val createdAt: Timestamp? = null
) {
    fun toDomain(): AppNotification {
        return AppNotification(
            id = documentId,
            title = title,
            body = body,
            isRead = isRead,
            createdAt = createdAt?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        )
    }

    companion object {
        fun fromDomain(notification: AppNotification): NotificationDto {
            return NotificationDto(
                documentId = notification.id,
                title = notification.title,
                body = notification.body,
                isRead = notification.isRead,
                createdAt = notification.createdAt
                    ?.atZone(ZoneId.systemDefault())
                    ?.toInstant()
                    ?.let { Timestamp(it.epochSecond, it.nano) }
            )
        }
    }
}
