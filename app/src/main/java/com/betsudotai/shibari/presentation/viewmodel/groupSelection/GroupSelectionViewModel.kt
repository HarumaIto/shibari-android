package com.betsudotai.shibari.presentation.viewmodel.groupSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.GroupRepository
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
class GroupSelectionViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSelectionUiState())
    val uiState: StateFlow<GroupSelectionUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<GroupSelectionEvent>()
    val eventFlow: SharedFlow<GroupSelectionEvent> = _eventFlow.asSharedFlow()

    fun createGroup(name: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _eventFlow.emit(GroupSelectionEvent.ShowError("ログインしてください"))
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val result = groupRepository.createGroup(name, description, uid)
            result.onSuccess { group ->
                userRepository.updateUserGroupId(uid, group.id).onSuccess {
                    _eventFlow.emit(GroupSelectionEvent.NavigateToQuestSelection)
                }.onFailure { error ->
                    _eventFlow.emit(GroupSelectionEvent.ShowError(error.message ?: "Failed to update user's group ID."))
                }
            }.onFailure { exception ->
                _eventFlow.emit(GroupSelectionEvent.ShowError(exception.message ?: "Failed to create group."))
            }
            _uiState.update { it.copy(isLoading = false,) }
        }
    }

    fun joinGroup(invitationCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _eventFlow.emit(GroupSelectionEvent.ShowError("User not logged in."))
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val result = groupRepository.getGroupByInvitationCode(invitationCode)
            result.onSuccess { group ->
                if (group != null) {
                    groupRepository.joinGroup(group.id, uid).onSuccess {
                        userRepository.updateUserGroupId(uid, group.id).onSuccess {
                            _eventFlow.emit(GroupSelectionEvent.NavigateToQuestSelection)
                        }.onFailure { error ->
                            _eventFlow.emit(GroupSelectionEvent.ShowError(error.message ?: "Failed to update user's group ID."))
                        }
                    }.onFailure { error ->
                        _eventFlow.emit(GroupSelectionEvent.ShowError(error.message ?: "Failed to join group."))
                    }
                } else {
                    _eventFlow.emit(GroupSelectionEvent.ShowError("Group not found with this invitation code."))
                }
            }.onFailure { error ->
                _eventFlow.emit(GroupSelectionEvent.ShowError(error.message ?: "Failed to find group by invitation code."))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
