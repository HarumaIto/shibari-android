package com.betsudotai.shibari.presentation.viewmodel.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.value.VoteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: TimelineRepository
) : ViewModel() {

    // RepositoryのFlowを監視し、UIStateに変換して保持する
    val uiState: StateFlow<TimelineUiState> = repository.getTimelineStream()
        .map { posts -> TimelineUiState.Success(posts) as TimelineUiState }
        .catch { emit(TimelineUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimelineUiState.Loading
        )

    fun vote(postId: String, voteType: VoteType) {
        viewModelScope.launch {
            // ※ 本来はAuthRepositoryから自分のIDを取得するが、今は仮ID
            val myUserId = "current_user_id_placeholder"

            repository.votePost(postId, myUserId, voteType)
                .onFailure {
                    // エラー処理（本来はSnackbarなどで通知）
                    it.printStackTrace()
                }
        }
    }
}