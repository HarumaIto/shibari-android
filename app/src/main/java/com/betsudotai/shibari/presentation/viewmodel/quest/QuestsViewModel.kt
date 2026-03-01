package com.betsudotai.shibari.presentation.viewmodel.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.value.QuestFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import javax.inject.Inject

@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val timelineRepository: TimelineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestsUiState>(QuestsUiState.Loading)
    val uiState: StateFlow<QuestsUiState> = _uiState.asStateFlow()

    init {
        loadMyQuests()
    }

    fun loadMyQuests() {
        viewModelScope.launch {
            _uiState.value = QuestsUiState.Loading
            try {
                val uid = authRepository.getCurrentUserId()
                if (uid == null) {
                    _uiState.value = QuestsUiState.Error("ログインしていません")
                    return@launch
                }

                val user = userRepository.getUser(uid)
                if (user == null) {
                    _uiState.value = QuestsUiState.Error("ユーザー情報が見つかりません")
                    return@launch
                }

                val groupId = user.groupId
                if (groupId == null) {
                    _uiState.value = QuestsUiState.Error("グループに所属していません")
                    return@launch
                }

                val questIds = user.participatingQuestIds
                if (questIds.isEmpty()) {
                    // 参加中の縛りがない場合
                    _uiState.value = QuestsUiState.Success(emptyList())
                    return@launch
                }

                // 全クエストを取得し、自分が参加しているものだけをフィルタリングする
                val allQuests = questRepository.getAllQuests(groupId) // Pass groupId
                val myQuests = allQuests.filter { questIds.contains(it.id) }

                // 各クエストの達成状況を確認する（1回のクエリでまとめて取得）
                val myQuestIds = myQuests.map { it.id }
                val myPosts = timelineRepository.getMyPostsForQuests(uid, myQuestIds)
                val achievedQuestIds = myQuests
                    .filter { quest ->
                        myPosts.any { post ->
                            post.questId == quest.id &&
                                post.createdAt?.let { isInCurrentPeriod(it, quest.frequency) } == true
                        }
                    }
                    .map { it.id }
                    .toSet()

                val groupedQuests = myQuests
                    .groupBy { it.frequency }
                    .map { (key, value) ->
                        QuestGroup(frequency = key, quests = value)
                    }
                    .sortedBy { it.frequency.sortOrder() }

                _uiState.value = QuestsUiState.Success(groupedQuests, achievedQuestIds)

            } catch (e: Exception) {
                _uiState.value = QuestsUiState.Error(e.message ?: "データの読み込みに失敗しました")
            }
        }
    }

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