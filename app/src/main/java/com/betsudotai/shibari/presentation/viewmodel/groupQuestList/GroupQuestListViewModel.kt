package com.betsudotai.shibari.presentation.viewmodel.groupQuestList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupQuestListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupQuestListUiState())
    val uiState: StateFlow<GroupQuestListUiState> = _uiState.asStateFlow()

    private var groupId: String? = null

    init {
        loadQuests()
    }

    fun loadQuests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val uid = authRepository.getCurrentUserId() ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "ログインしていません") }
                    return@launch
                }
                val user = userRepository.getUser(uid) ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "ユーザー情報が見つかりません") }
                    return@launch
                }
                val gId = user.groupId ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "グループに所属していません") }
                    return@launch
                }
                groupId = gId
                val quests = questRepository.getAllQuests(gId)
                _uiState.update { it.copy(isLoading = false, quests = quests) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "データの読み込みに失敗しました") }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                val gId = groupId ?: run {
                    _uiState.update { it.copy(isRefreshing = false) }
                    return@launch
                }
                val quests = questRepository.getAllQuests(gId)
                _uiState.update { it.copy(isRefreshing = false, quests = quests) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, error = e.message ?: "データの読み込みに失敗しました") }
            }
        }
    }
}
