package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.QuestRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.TimelineRemoteDataSource
import com.betsudotai.shibari.data.datasource.remote.UserRemoteDataSource
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.value.QuestFrequency
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val remoteDataSource: QuestRemoteDataSource,
    private val userDataSource: UserRemoteDataSource,
    private val timelineDataSource: TimelineRemoteDataSource
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

    override suspend fun getMyQuests(userId: String, groupId: String): List<Quest> {
        // 1. ユーザーの参加クエストIDを取得
        val userDto = userDataSource.getUser(userId) ?: return emptyList()
        val participatingQuestIds = userDto.participatingQuestIds

        if (participatingQuestIds.isEmpty()) return emptyList()

        // 2. グループのクエストを取得し、参加しているものだけをフィルタリング
        val allQuests = remoteDataSource.fetchAllQuests(groupId)
        val myQuestDtos = allQuests.filter { it.documentId in participatingQuestIds }

        if (myQuestDtos.isEmpty()) return emptyList()

        // 3. 期間内の自分の投稿をまとめて取得
        val myQuestIds = myQuestDtos.map { it.documentId }
        val myPosts = timelineDataSource.getMyPostsForQuests(userId, myQuestIds)

        // 4. 各クエストの達成状況を判定してDomainモデルを生成
        return myQuestDtos.map { questDto ->
            val quest = questDto.toDomain()
            val isCompleted = myPosts.any { post ->
                post.questId == quest.id &&
                    post.createdAt?.toLocalDate()?.let { isInCurrentPeriod(it, quest.frequency) } == true
            }
            quest.copy(isCompleted = isCompleted)
        }
    }

    private fun com.google.firebase.Timestamp.toLocalDate(): LocalDate =
        toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    private fun isInCurrentPeriod(date: LocalDate, frequency: QuestFrequency): Boolean {
        val today = LocalDate.now()
        return when (frequency) {
            QuestFrequency.ALWAYS -> true
            QuestFrequency.DAILY -> date == today
            QuestFrequency.WEEKLY -> {
                val weekFields = WeekFields.ISO
                date.year == today.year &&
                    date.get(weekFields.weekOfWeekBasedYear()) == today.get(weekFields.weekOfWeekBasedYear())
            }
            QuestFrequency.MONTHLY -> date.year == today.year && date.monthValue == today.monthValue
            QuestFrequency.YEARLY -> date.year == today.year
        }
    }
}