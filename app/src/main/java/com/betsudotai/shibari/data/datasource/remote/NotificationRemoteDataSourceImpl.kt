package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.NotificationDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRemoteDataSource {

    private fun userNotificationsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("notifications")

    override suspend fun getNotifications(userId: String): List<NotificationDto> {
        val snapshot = userNotificationsCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(NotificationDto::class.java) }
    }

    override suspend fun markAllAsRead(userId: String, notificationIds: List<String>) {
        if (notificationIds.isEmpty()) return // Avoid an unnecessary Firestore batch commit round-trip
        val batch = firestore.batch()
        val collection = userNotificationsCollection(userId)
        notificationIds.forEach { id ->
            batch.update(collection.document(id), "isRead", true)
        }
        batch.commit().await()
    }
}
