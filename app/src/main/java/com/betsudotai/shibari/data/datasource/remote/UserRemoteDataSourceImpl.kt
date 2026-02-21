package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
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
}