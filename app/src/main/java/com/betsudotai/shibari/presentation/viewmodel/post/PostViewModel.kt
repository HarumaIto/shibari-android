package com.betsudotai.shibari.presentation.viewmodel.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.core.util.FileUtil
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.repository.UserRepository // Import UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // Correct import for Inject
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
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository // Inject UserRepository
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
            viewModelScope.launch { _eventFlow.emit(PostEvent.ShowError("メディアを選択してください")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val userId = authRepository.getCurrentUserId()

            if (userId == null) {
                _eventFlow.emit(PostEvent.ShowError("認証エラーです。再ログインしてください"))
                _isLoading.value = false
                return@launch
            }

            val currentUser = userRepository.getUser(userId) // Get current user to access groupId
            val groupId = currentUser?.groupId

            if (groupId == null) {
                _eventFlow.emit(PostEvent.ShowError("グループに所属していません。"))
                _isLoading.value = false
                return@launch
            }

            // Uri から File に変換（ここで画像なら圧縮される）
            val mediaFile = FileUtil.createTempFileFromUri(context, uri)
            if (mediaFile == null) {
                _eventFlow.emit(PostEvent.ShowError("ファイルの読み込みに失敗しました"))
                _isLoading.value = false
                return@launch
            }

            // MIMEタイプから "image" か "video" かを判定
            val mimeType = context.contentResolver.getType(uri)
            val typeString = if (mimeType?.startsWith("video/") == true) "video" else "image"

            // ★追加: 動画のファイルサイズチェック（50MB = 50 * 1024 * 1024 バイト）
            val maxVideoSizeBytes = 50L * 1024 * 1024
            if (typeString == "video" && mediaFile.length() > maxVideoSizeBytes) {
                // ファイルサイズをMB単位で計算してエラーメッセージに含める
                val actualSizeMb = mediaFile.length() / (1024 * 1024)
                _eventFlow.emit(PostEvent.ShowError("動画が大きすぎます（${actualSizeMb}MB）。50MB以下の動画を選んでください。"))

                // 無駄に容量を食わないよう、一時ファイルを削除しておく
                mediaFile.delete()
                _isLoading.value = false
                return@launch
            }

            // Repositoryの投稿処理を呼び出す
            val result = timelineRepository.createPost(
                userId = userId,
                questId = questId,
                groupId = groupId, // Pass groupId
                mediaFile = mediaFile,
                mediaType = typeString,
                comment = _comment.value
            )

            result.onSuccess {
                _eventFlow.emit(PostEvent.NavigateBack)
            }.onFailure { error ->
                // ★追加: ネットワークエラーなどの例外を、ユーザーに分かりやすい言葉に翻訳する
                val errorMessage = when {
                    error.message?.contains("network", ignoreCase = true) == true -> "通信環境の良いところで再度お試しください"
                    error.message?.contains("permission", ignoreCase = true) == true -> "アップロードが許可されませんでした"
                    else -> "投稿に失敗しました: ${error.localizedMessage}"
                }
                _eventFlow.emit(PostEvent.ShowError(errorMessage))
            }

            // 投稿成功・失敗に関わらず、最後にキャッシュの一時ファイルを削除してスマホの容量を空ける
            if (mediaFile.exists()) {
                mediaFile.delete()
            }

            _isLoading.value = false
        }
    }
}