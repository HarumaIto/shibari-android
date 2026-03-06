package com.betsudotai.shibari.domain.model

import com.betsudotai.shibari.domain.value.NotificationType
import java.time.LocalDateTime

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val senderId: String?,
    val targetId: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime?
)
