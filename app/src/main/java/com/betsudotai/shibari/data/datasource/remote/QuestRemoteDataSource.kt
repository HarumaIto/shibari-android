package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.QuestDto

interface QuestRemoteDataSource {
    suspend fun fetchAllQuests(): List<QuestDto>
    suspend fun fetchQuest(id: String): QuestDto?
}