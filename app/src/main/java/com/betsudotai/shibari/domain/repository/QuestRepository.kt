package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.Quest

interface QuestRepository {
    suspend fun getAllQuests(): List<Quest>
    suspend fun getQuest(id: String): Quest?
}