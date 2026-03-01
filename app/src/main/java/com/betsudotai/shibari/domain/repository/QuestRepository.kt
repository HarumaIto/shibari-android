package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.Quest

interface QuestRepository {
    suspend fun getAllQuests(groupId: String): List<Quest>
    suspend fun getQuest(id: String): Quest?
    suspend fun getMyQuests(userId: String, groupId: String): List<Quest>
}