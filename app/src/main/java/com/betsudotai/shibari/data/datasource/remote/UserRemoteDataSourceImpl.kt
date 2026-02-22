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

    override suspend fun getUser(userId: String): UserDto? {
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.toObject(UserDto::class.java)
    }

    override suspend fun createUser(userDto: UserDto) {
        firestore.collection("users").document(userDto.id).set(userDto).await()
    }

    override suspend fun updateQuests(userId: String, questIds: List<String>) {
        firestore.collection("users").document(userId)
            .update("participatingQuestIds", questIds).await()
    }

    override suspend fun updateProfile(userId: String, displayName: String, photoUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "displayName" to displayName
        )
        if (photoUrl != null) {
            updates["photoUrl"] = photoUrl
        }
        firestore.collection("users").document(userId).update(updates).await()
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
        firestore.collection("users").document(userId)
            .update("fcmToken", token).await()
    }

    override suspend fun updateUserGroupId(userId: String, groupId: String?) {
        firestore.collection("users").document(userId)
            .update("groupId", groupId).await()
    }
}