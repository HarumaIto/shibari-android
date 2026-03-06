package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.dto.QuestDto
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuestRemoteDataSource
) : QuestRepository {

    override suspend fun getAllQuests(groupId: String): List<Quest> {
        return try {
            // DataSourceからDTOをもらい、Domainモデルに変換する
            remoteDataSource.fetchAllQuests(groupId).map { it.toDomain() }
        } catch (e: Exception) {
            // エラー時は空リスト（またはResult型でラップする設計もアリ）
            emptyList()
        }
    }

    override suspend fun getQuest(id: String): Quest? {
        return try {
            remoteDataSource.fetchQuest(id)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createQuest(
        groupId: String,
        title: String,
        description: String,
        type: QuestType,
        frequency: QuestFrequency,
        threshold: Int?
    ): Result<Quest> {
        return runCatching {
            val dto = QuestDto(
                groupId = groupId,
                title = title,
                description = description,
                type = type.name,
                frequency = frequency.name,
                threshold = threshold
            )
            remoteDataSource.createQuest(dto).toDomain()
        }
    }

    override suspend fun updateQuest(quest: Quest): Result<Quest> {
        return runCatching {
            val dto = QuestDto(
                documentId = quest.id,
                groupId = quest.groupId,
                title = quest.title,
                description = quest.description,
                type = quest.type.name,
                frequency = quest.frequency.name,
                threshold = quest.threshold
            )
            remoteDataSource.updateQuest(dto)
            quest
        }
    }
}