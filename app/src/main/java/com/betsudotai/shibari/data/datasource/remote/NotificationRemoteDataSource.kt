package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.NotificationDto
import kotlinx.coroutines.flow.Flow

interface NotificationRemoteDataSource {
    fun getNotificationsStream(userId: String): Flow<List<NotificationDto>>
    suspend fun markAsRead(userId: String, notificationId: String)
    suspend fun saveNotification(userId: String, notification: NotificationDto)
}
