package com.betsudotai.shibari.presentation.viewmodel.questCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.QuestRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestCreationViewModel @Inject constructor(
    private val questRepository: QuestRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedType = MutableStateFlow(QuestType.ROUTINE)
    val selectedType: StateFlow<QuestType> = _selectedType.asStateFlow()

    private val _selectedFrequency = MutableStateFlow(QuestFrequency.DAILY)
    val selectedFrequency: StateFlow<QuestFrequency> = _selectedFrequency.asStateFlow()

    private val _threshold = MutableStateFlow("1")
    val threshold: StateFlow<String> = _threshold.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<QuestCreationEvent>()
    val eventFlow: SharedFlow<QuestCreationEvent> = _eventFlow.asSharedFlow()

    fun onTitleChange(value: String) { _title.value = value }

    fun onDescriptionChange(value: String) { _description.value = value }

    fun onTypeChange(value: QuestType) {
        _selectedType.value = value
        if (value == QuestType.ACHIEVEMENT) {
            _selectedFrequency.value = QuestFrequency.ALWAYS
        }
    }

    fun onFrequencyChange(value: QuestFrequency) {
        if (_selectedType.value != QuestType.ACHIEVEMENT) {
            _selectedFrequency.value = value
        }
    }

    fun onThresholdChange(value: String) {
        if (value.all { it.isDigit() }) {
            _threshold.value = value
        }
    }

    fun createQuest() {
        viewModelScope.launch {
            _isLoading.value = true

            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _eventFlow.emit(QuestCreationEvent.ShowError("認証エラーです"))
                _isLoading.value = false
                return@launch
            }

            val user = runCatching { userRepository.getUser(uid) }.getOrElse {
                _eventFlow.emit(QuestCreationEvent.ShowError("ユーザー情報の取得に失敗しました"))
                _isLoading.value = false
                return@launch
            }
            val groupId = user?.groupId
            if (groupId.isNullOrEmpty()) {
                _eventFlow.emit(QuestCreationEvent.ShowError("グループが見つかりません"))
                _isLoading.value = false
                return@launch
            }

            val quest = Quest(
                groupId = groupId,
                title = _title.value.trim(),
                type = _selectedType.value,
                frequency = _selectedFrequency.value,
                description = _description.value.trim(),
                threshold = _threshold.value.toIntOrNull()
            )

            val result = questRepository.createQuest(quest)
            result.onSuccess {
                _eventFlow.emit(QuestCreationEvent.NavigateBack)
            }.onFailure {
                _eventFlow.emit(QuestCreationEvent.ShowError("作成に失敗しました"))
            }
            _isLoading.value = false
        }
    }
}
