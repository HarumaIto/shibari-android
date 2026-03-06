package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.AppNotification

interface NotificationRepository {
    suspend fun getNotifications(userId: String): Result<List<AppNotification>>
    suspend fun markAllAsRead(userId: String, notificationIds: List<String>): Result<Unit>
}
