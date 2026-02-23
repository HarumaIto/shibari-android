package com.betsudotai.shibari.presentation.viewmodel.profileEdit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.core.util.FileUtil
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.UserRepository
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
class ProfileEditViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    // 現在のアイコンURL（プレビュー用）
    private val _currentPhotoUrl = MutableStateFlow<String?>(null)
    val currentPhotoUrl: StateFlow<String?> = _currentPhotoUrl.asStateFlow()

    // 新しく選択した画像のURI
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEditEvent>()
    val eventFlow: SharedFlow<ProfileEditEvent> = _eventFlow.asSharedFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val uid = authRepository.getCurrentUserId() ?: return@launch
            val user = userRepository.getUser(uid)
            if (user != null) {
                _displayName.value = user.displayName
                _currentPhotoUrl.value = user.photoUrl
            }
            _isLoading.value = false
        }
    }

    fun onNameChange(name: String) { _displayName.value = name }
    fun onImageSelected(uri: Uri?) { _selectedImageUri.value = uri }

    fun saveProfile(context: Context) {
        val name = _displayName.value.trim()
        if (name.isEmpty()) {
            viewModelScope.launch { _eventFlow.emit(ProfileEditEvent.ShowError("名前を入力してください")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _eventFlow.emit(ProfileEditEvent.ShowError("認証エラーです"))
                _isLoading.value = false
                return@launch
            }

            // 画像が選択されていれば File に変換
            val photoFile = _selectedImageUri.value?.let { uri ->
                FileUtil.createTempFileFromUri(context, uri)
            }

            val result = userRepository.updateProfile(uid, name, photoFile)
            result.onSuccess {
                _eventFlow.emit(ProfileEditEvent.NavigateBack)
            }.onFailure {
                _eventFlow.emit(ProfileEditEvent.ShowError("保存に失敗しました"))
            }

            _isLoading.value = false
        }
    }
}