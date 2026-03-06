package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType

interface QuestRepository {
    suspend fun getAllQuests(groupId: String): List<Quest>
    suspend fun getQuest(id: String): Quest?
    suspend fun createQuest(
        groupId: String,
        title: String,
        description: String,
        type: QuestType,
        frequency: QuestFrequency,
        threshold: Int?
    ): Result<Quest>
    suspend fun updateQuest(quest: Quest): Result<Quest>
}