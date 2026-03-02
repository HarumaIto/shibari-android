package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.dto.QuestDto
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.QuestRepository
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

    override suspend fun createQuest(quest: Quest): Result<Unit> {
        return try {
            val dto = QuestDto(
                groupId = quest.groupId,
                title = quest.title,
                type = quest.type.name,
                frequency = quest.frequency.name,
                description = quest.description,
                threshold = quest.threshold
            )
            remoteDataSource.createQuest(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}