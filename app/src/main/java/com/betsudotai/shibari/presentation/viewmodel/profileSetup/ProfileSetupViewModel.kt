package com.betsudotai.shibari.presentation.viewmodel.profileSetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.model.User
import com.betsudotai.shibari.domain.repository.AuthRepository
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
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileSetupEvent>()
    val eventFlow: SharedFlow<ProfileSetupEvent> = _eventFlow.asSharedFlow()

    fun onNameChange(name: String) { _displayName.update { name } }

    fun saveProfile() {
        if (_displayName.value.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(ProfileSetupEvent.ShowError("名前を入力してください")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val uid = authRepository.getCurrentUserId()

            if (uid == null) {
                _eventFlow.emit(ProfileSetupEvent.ShowError("認証エラー: 再ログインしてください"))
                _isLoading.value = false
                return@launch
            }

            // 新規ユーザーモデルの作成
            val newUser = User(
                uid = uid,
                displayName = _displayName.value,
                photoUrl = null, // V1.1では後回し（デフォルトアイコン）でも可
                fcmToken = null,
                participatingQuestIds = emptyList() // 次の「縛り選択画面」で追加する
            )

            val result = userRepository.createUser(newUser)
            result.onSuccess {
                _eventFlow.emit(ProfileSetupEvent.NavigateToMain)
            }.onFailure {
                _eventFlow.emit(ProfileSetupEvent.ShowError("保存に失敗しました"))
            }

            _isLoading.value = false
        }
    }
}