package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.NotificationDto

interface NotificationRemoteDataSource {
    suspend fun getNotifications(userId: String): List<NotificationDto>
    suspend fun markAllAsRead(userId: String, notificationIds: List<String>)
}
