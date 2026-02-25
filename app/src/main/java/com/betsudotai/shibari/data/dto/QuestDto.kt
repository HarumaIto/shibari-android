package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

// Firestoreのドキュメント構造と完全一致させる
data class QuestDto(
    @DocumentId val documentId: String = "",
    @PropertyName("groupId") val groupId: String = "", // Add groupId here
    @PropertyName("title") val title: String = "",
    @PropertyName("type") val type: String = "", // EnumではなくStringで保存
    @PropertyName("frequency") val frequency: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("threshold") val threshold: Int? = null
) {
    // DTO -> Domain Model への変換メソッド
    fun toDomain(): Quest {
        return Quest(
            id = documentId,
            groupId = groupId, // Map groupId here
            title = title,
            type = try {
                QuestType.valueOf(type)
            } catch (e: Exception) {
                QuestType.ROUTINE // フォールバック
            },
            frequency = try {
                QuestFrequency.valueOf(frequency)
            } catch (e: Exception) {
                QuestFrequency.ALWAYS
            },
            description = description,
            threshold = threshold
        )
    }
}