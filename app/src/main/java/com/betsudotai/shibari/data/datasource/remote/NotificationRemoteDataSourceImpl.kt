package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.NotificationDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRemoteDataSource {

    private fun userNotificationsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("notifications")

    override fun getNotificationsStream(userId: String): Flow<List<NotificationDto>> = callbackFlow {
        val collection = userNotificationsCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val notifications = snapshot.documents.mapNotNull { it.toObject(NotificationDto::class.java) }
                trySend(notifications)
            }
        }

        awaitClose { registration.remove() }
    }

    override suspend fun markAsRead(userId: String, notificationId: String) {
        userNotificationsCollection(userId).document(notificationId)
            .update("isRead", true).await()
    }

    override suspend fun saveNotification(userId: String, notification: NotificationDto) {
        userNotificationsCollection(userId).document(notification.documentId).set(notification).await()
    }
}
