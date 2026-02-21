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

                val questIds = user.participatingQuestIds
                if (questIds.isEmpty()) {
                    // 参加中の縛りがない場合
                    _uiState.value = QuestsUiState.Success(emptyList())
                    return@launch
                }

                // 全クエストを取得し、自分が参加しているものだけをフィルタリングする
                // （※データ量が増えてきたらFirestore側でin句を使って取得する設計に変更しますが、現状はこれで十分高速です）
                val allQuests = questRepository.getAllQuests()
                val myQuests = allQuests.filter { questIds.contains(it.id) }

                _uiState.value = QuestsUiState.Success(myQuests)

            } catch (e: Exception) {
                _uiState.value = QuestsUiState.Error(e.message ?: "データの読み込みに失敗しました")
            }
        }
    }
}