package com.betsudotai.shibari.presentation.viewmodel.comment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.model.Comment
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val timelineRepository: TimelineRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // 前の画面から postId を受け取る
    private val postId: String = checkNotNull(savedStateHandle["postId"])

    // コメント一覧をリアルタイム監視
    val comments: StateFlow<List<Comment>> = timelineRepository.getCommentsStream(postId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendComment() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _isSending.value = true
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = timelineRepository.addComment(postId, userId, text)
                if (result.isSuccess) {
                    _inputText.value = "" // 送信成功したら入力欄をクリア
                }
            }
            _isSending.value = false
        }
    }
}