package com.betsudotai.shibari.presentation.viewmodel.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.core.util.FileUtil
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val timelineRepository: TimelineRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // 遷移元の画面から "questId" を受け取る
    private val questId: String = checkNotNull(savedStateHandle["questId"])

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PostEvent>()
    val eventFlow: SharedFlow<PostEvent> = _eventFlow.asSharedFlow()

    fun onImageSelected(uri: Uri?) { _selectedImageUri.value = uri }
    fun onCommentChange(text: String) { _comment.value = text }

    fun submitPost(context: Context) {
        val uri = _selectedImageUri.value
        if (uri == null) {
            viewModelScope.launch { _eventFlow.emit(PostEvent.ShowError("画像を選択してください")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()

            if (userId == null) {
                _eventFlow.emit(PostEvent.ShowError("認証エラーです"))
                _isLoading.value = false
                return@launch
            }

            // Uri から File に変換
            val imageFile = FileUtil.createTempFileFromUri(context, uri)
            if (imageFile == null) {
                _eventFlow.emit(PostEvent.ShowError("画像の読み込みに失敗しました"))
                _isLoading.value = false
                return@launch
            }

            // Repositoryの投稿処理を呼び出す
            val result = timelineRepository.createPost(
                userId = userId,
                questId = questId,
                mediaFile = imageFile,
                mediaType = "image", // 今回は画像固定
                comment = _comment.value
            )

            result.onSuccess {
                // アップロード完了したら前の画面に戻る
                _eventFlow.emit(PostEvent.NavigateBack)
            }.onFailure {
                _eventFlow.emit(PostEvent.ShowError("投稿に失敗しました: ${it.message}"))
            }

            _isLoading.value = false
        }
    }
}