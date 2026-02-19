package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.QuestRepository
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuestRemoteDataSource
) : QuestRepository {

    override suspend fun getAllQuests(): List<Quest> {
        return try {
            // DataSourceからDTOをもらい、Domainモデルに変換する
            remoteDataSource.fetchAllQuests().map { it.toDomain() }
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
}