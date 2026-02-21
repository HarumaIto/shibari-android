package com.betsudotai.shibari.presentation.viewmodel.questSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestSelectionViewModel @Inject constructor(
    private val questRepository: QuestRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _selectedQuestIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedQuestIds: StateFlow<Set<String>> = _selectedQuestIds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<QuestSelectionEvent>()
    val eventFlow: SharedFlow<QuestSelectionEvent> = _eventFlow.asSharedFlow()

    init {
        loadQuests()
    }

    private fun loadQuests() {
        viewModelScope.launch {
            _isLoading.value = true
            // 全クエスト一覧を取得
            val allQuests = questRepository.getAllQuests()
            _quests.value = allQuests

            // 現在のユーザー情報を取得し、すでに選んでいるクエストがあればチェックを入れる
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                val user = userRepository.getUser(uid)
                if (user != null) {
                    _selectedQuestIds.value = user.participatingQuestIds.toSet()
                }
            }
            _isLoading.value = false
        }
    }

    fun toggleQuest(questId: String) {
        _selectedQuestIds.update { current ->
            if (current.contains(questId)) {
                current - questId
            } else {
                current + questId
            }
        }
    }

    fun saveSelection() {
        viewModelScope.launch {
            _isLoading.value = true
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _eventFlow.emit(QuestSelectionEvent.ShowError("認証エラーです"))
                _isLoading.value = false
                return@launch
            }

            val result = userRepository.updateParticipatingQuests(uid, _selectedQuestIds.value.toList())
            result.onSuccess {
                _eventFlow.emit(QuestSelectionEvent.NavigateToMain)
            }.onFailure {
                _eventFlow.emit(QuestSelectionEvent.ShowError("保存に失敗しました"))
            }
            _isLoading.value = false
        }
    }
}