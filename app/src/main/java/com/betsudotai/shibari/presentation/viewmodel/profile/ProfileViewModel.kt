package com.betsudotai.shibari.presentation.viewmodel.profile

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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val uid = authRepository.getCurrentUserId()
                if (uid == null) {
                    _uiState.value = ProfileUiState.Error("ログインしていません")
                    return@launch
                }

                val user = userRepository.getUser(uid)
                if (user == null) {
                    _uiState.value = ProfileUiState.Error("ユーザー情報が見つかりません")
                    return@launch
                }

                // 参加中のクエスト詳細を取得
                val allQuests = questRepository.getAllQuests(user.groupId!!)
                val myQuests = allQuests.filter { user.participatingQuestIds.contains(it.id) }

                _uiState.value = ProfileUiState.Success(user, myQuests)

            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "データの読み込みに失敗しました")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}