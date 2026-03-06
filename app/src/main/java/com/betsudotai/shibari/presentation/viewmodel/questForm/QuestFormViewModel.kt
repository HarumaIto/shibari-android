package com.betsudotai.shibari.presentation.viewmodel.questForm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository
) : ViewModel() {

    private val questId: String? = savedStateHandle.get<String>("questId")

    private val _uiState = MutableStateFlow(QuestFormUiState())
    val uiState: StateFlow<QuestFormUiState> = _uiState.asStateFlow()

    private var groupId: String? = null

    init {
        if (questId != null) {
            loadQuest()
        } else {
            _uiState.update { it.copy(isEditMode = false) }
            loadGroupId()
        }
    }

    private fun loadGroupId() {
        viewModelScope.launch {
            try {
                val uid = authRepository.getCurrentUserId() ?: return@launch
                val user = userRepository.getUser(uid) ?: return@launch
                groupId = user.groupId
            } catch (e: Exception) {
                // Silently fail; save() will handle the missing groupId
            }
        }
    }

    private fun loadQuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val quest = questRepository.getQuest(questId!!) ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "縛り情報が見つかりません") }
                    return@launch
                }
                groupId = quest.groupId
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        title = quest.title,
                        description = quest.description,
                        type = quest.type,
                        frequency = quest.frequency,
                        thresholdText = quest.threshold?.toString() ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "データの読み込みに失敗しました") }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onTypeChange(value: QuestType) {
        _uiState.update { it.copy(type = value) }
    }

    fun onFrequencyChange(value: QuestFrequency) {
        _uiState.update { it.copy(frequency = value) }
    }

    fun onThresholdChange(value: String) {
        _uiState.update { it.copy(thresholdText = value) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val threshold = state.thresholdText.toIntOrNull()
                val gId = resolveGroupId() ?: run {
                    _uiState.update { it.copy(isSaving = false, error = "グループ情報が取得できません") }
                    return@launch
                }

                if (questId != null) {
                    val quest = Quest(
                        id = questId,
                        groupId = gId,
                        title = state.title,
                        description = state.description,
                        type = state.type,
                        frequency = state.frequency,
                        threshold = threshold
                    )
                    questRepository.updateQuest(quest).onSuccess {
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isSaving = false, error = e.message ?: "保存に失敗しました") }
                    }
                } else {
                    questRepository.createQuest(gId, state.title, state.description, state.type, state.frequency, threshold).onSuccess {
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                    }.onFailure { e ->
                        _uiState.update { it.copy(isSaving = false, error = e.message ?: "保存に失敗しました") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "保存に失敗しました") }
            }
        }
    }

    private suspend fun resolveGroupId(): String? {
        groupId?.let { return it }
        return try {
            val uid = authRepository.getCurrentUserId() ?: return null
            val user = userRepository.getUser(uid) ?: return null
            user.groupId.also { groupId = it }
        } catch (e: Exception) {
            null
        }
    }
}
