package com.betsudotai.shibari.presentation.viewmodel.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.GroupRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupUiState>(GroupUiState.Loading)
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    init {
        loadGroupData()
    }

    fun loadGroupData() {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            try {
                val uid = authRepository.getCurrentUserId() ?: run {
                    _uiState.value = GroupUiState.Error("ログインしていません")
                    return@launch
                }
                val user = userRepository.getUser(uid) ?: run {
                    _uiState.value = GroupUiState.Error("ユーザー情報が見つかりません")
                    return@launch
                }
                val groupId = user.groupId ?: run {
                    _uiState.value = GroupUiState.Error("グループに所属していません")
                    return@launch
                }
                val group = groupRepository.getGroupDetails(groupId).getOrNull() ?: run {
                    _uiState.value = GroupUiState.Error("グループ情報の取得に失敗しました")
                    return@launch
                }
                val members = group.memberIds.mapNotNull { memberId ->
                    userRepository.getUser(memberId)
                }
                _uiState.value = GroupUiState.Success(group, members)
            } catch (e: Exception) {
                _uiState.value = GroupUiState.Error(e.message ?: "データの読み込みに失敗しました")
            }
        }
    }
}
