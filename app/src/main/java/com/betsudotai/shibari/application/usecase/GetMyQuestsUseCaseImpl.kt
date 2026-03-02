package com.betsudotai.shibari.application.usecase

import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.usecase.GetMyQuestsUseCase
import com.betsudotai.shibari.domain.value.QuestFrequency
import java.time.LocalDate
import java.time.temporal.WeekFields
import javax.inject.Inject

class GetMyQuestsUseCaseImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val timelineRepository: TimelineRepository
) : GetMyQuestsUseCase {

    override suspend operator fun invoke(userId: String, groupId: String): List<Quest> {
        // 1. ユーザーの参加クエストIDを取得
        val user = userRepository.getUser(userId) ?: return emptyList()
        val participatingQuestIds = user.participatingQuestIds

        if (participatingQuestIds.isEmpty()) return emptyList()

        // 2. グループのクエストを取得し、参加しているものだけをフィルタリング
        val myQuests = questRepository.getAllQuests(groupId)
            .filter { it.id in participatingQuestIds }

        if (myQuests.isEmpty()) return emptyList()

        // 3. 期間内の自分の投稿をまとめて取得（N+1を回避）
        val myQuestIds = myQuests.map { it.id }
        val myPosts = timelineRepository.getMyPostsForQuests(userId, groupId, myQuestIds)

        // 4. 各クエストの達成状況を判定してDomainモデルを生成
        return myQuests.map { quest ->
            val isCompleted = myPosts.any { post ->
                post.questId == quest.id &&
                    post.createdAt?.let { isInCurrentPeriod(it, quest.frequency) } == true
            }
            quest.copy(isCompleted = isCompleted)
        }
    }

    private fun isInCurrentPeriod(date: LocalDate, frequency: QuestFrequency): Boolean {
        val today = LocalDate.now()
        return when (frequency) {
            QuestFrequency.ALWAYS -> false
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
