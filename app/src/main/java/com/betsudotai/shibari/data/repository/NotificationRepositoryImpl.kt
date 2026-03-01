package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.NotificationRemoteDataSource
import com.betsudotai.shibari.data.dto.NotificationDto
import com.betsudotai.shibari.domain.model.AppNotification
import com.betsudotai.shibari.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dataSource: NotificationRemoteDataSource
) : NotificationRepository {

    override fun getNotificationsStream(userId: String): Flow<List<AppNotification>> {
        return dataSource.getNotificationsStream(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> {
        return runCatching { dataSource.markAsRead(userId, notificationId) }
    }

    override suspend fun saveNotification(userId: String, notification: AppNotification): Result<Unit> {
        return runCatching {
            dataSource.saveNotification(userId, NotificationDto.fromDomain(notification))
        }
    }
}
