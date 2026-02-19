package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.QuestDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class QuestRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuestRemoteDataSource {

    override suspend fun fetchAllQuests(): List<QuestDto> {
        return try {
            val snapshot = firestore.collection("quests").get().await()
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
}