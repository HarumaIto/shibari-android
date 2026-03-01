package com.betsudotai.shibari.presentation.viewmodel.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository
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

                // 参加中クエストと達成状況をリポジトリで一括取得
                val myQuests = questRepository.getMyQuests(uid, groupId)

                if (myQuests.isEmpty()) {
                    _uiState.value = QuestsUiState.Success(emptyList())
                    return@launch
                }

                val groupedQuests = myQuests
                    .groupBy { it.frequency }
                    .map { (key, value) ->
                        QuestGroup(frequency = key, quests = value)
                    }
                    .sortedBy { it.frequency.sortOrder() }

                _uiState.value = QuestsUiState.Success(groupedQuests)

            } catch (e: Exception) {
                _uiState.value = QuestsUiState.Error(e.message ?: "データの読み込みに失敗しました")
            }
        }
    }
}