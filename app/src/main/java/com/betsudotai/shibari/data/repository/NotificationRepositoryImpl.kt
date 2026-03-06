package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.NotificationRemoteDataSource
import com.betsudotai.shibari.domain.model.AppNotification
import com.betsudotai.shibari.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dataSource: NotificationRemoteDataSource
) : NotificationRepository {

    override suspend fun getNotifications(userId: String): Result<List<AppNotification>> {
        return runCatching {
            dataSource.getNotifications(userId).map { it.toDomain() }
        }
    }

    override suspend fun markAllAsRead(userId: String, notificationIds: List<String>): Result<Unit> {
        return runCatching { dataSource.markAllAsRead(userId, notificationIds) }
    }
}
