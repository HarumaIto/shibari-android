package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsStream(userId: String): Flow<List<AppNotification>>
    suspend fun markAsRead(userId: String, notificationId: String): Result<Unit>
    suspend fun saveNotification(userId: String, notification: AppNotification): Result<Unit>
}
