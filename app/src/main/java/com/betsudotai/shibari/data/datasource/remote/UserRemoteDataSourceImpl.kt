package com.betsudotai.shibari.data.datasource.remote

import android.net.Uri
import com.betsudotai.shibari.data.dto.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRemoteDataSource {

    private val usersCollection = firestore.collection("users")

    override suspend fun getUser(userId: String): UserDto? {
        val doc = usersCollection.document(userId).get().await()
        return doc.toObject(UserDto::class.java)
    }

    override suspend fun createUser(userDto: UserDto) {
        usersCollection.document(userDto.documentId).set(userDto).await()
    }

    override suspend fun updateUser(userId: String, data: Map<String, Any?>) {
        usersCollection.document(userId).update(data).await()
    }

    override suspend fun updateQuests(userId: String, questIds: List<String>) {
        usersCollection.document(userId)
            .update("participatingQuestIds", questIds).await()
    }

    override suspend fun updateProfile(userId: String, displayName: String, photoUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "displayName" to displayName
        )
        if (photoUrl != null) {
            updates["photoUrl"] = photoUrl
        }
        usersCollection.document(userId).update(updates).await()
    }

    override suspend fun uploadProfileImage(userId: String, file: File): String {
        // 先ほど設定したルールの通り profiles/{userId}/{imageId} に保存します
        val path = "profiles/$userId/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(path)
        val uri = Uri.fromFile(file)

        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun updateFcmToken(userId: String, token: String) {
        usersCollection.document(userId)
            .update("fcmToken", token).await()
    }

    override suspend fun updateUserGroupId(userId: String, groupId: String?) {
        usersCollection.document(userId)
            .update("groupId", groupId).await()
    }

    override suspend fun blockUser(currentUserId: String, targetUserId: String) {
        usersCollection.document(currentUserId)
            .update("blockedUserIds", com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId))
            .await()
    }
}