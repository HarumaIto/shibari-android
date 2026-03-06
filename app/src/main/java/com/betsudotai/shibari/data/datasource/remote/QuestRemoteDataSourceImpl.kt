package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.QuestDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class QuestRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuestRemoteDataSource {

    override suspend fun fetchAllQuests(groupId: String): List<QuestDto> {
        return try {
            val snapshot = firestore.collection("quests")
                .whereEqualTo("groupId", groupId) // Filter quests by groupId
                .get().await()
            // ここでは単にDTOを返すだけ（ドメインロジックは持たない）
            snapshot.documents.mapNotNull { it.toObject(QuestDto::class.java) }
        } catch (e: Exception) {
            // エラーハンドリング方針によるが、一旦空リストかThrow
            throw e
        }
    }

    override suspend fun fetchQuest(id: String): QuestDto? {
        val doc = firestore.collection("quests").document(id).get().await()
        return doc.toObject(QuestDto::class.java)
    }

    override suspend fun createQuest(questDto: QuestDto): QuestDto {
        val ref = firestore.collection("quests").document()
        ref.set(questDto).await()
        return questDto.copy(documentId = ref.id)
    }

    override suspend fun updateQuest(questDto: QuestDto) {
        require(questDto.documentId.isNotEmpty()) { "Quest documentId must not be empty for update" }
        firestore.collection("quests").document(questDto.documentId).set(questDto).await()
    }
}