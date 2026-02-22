package com.betsudotai.shibari.presentation.viewmodel.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.value.VoteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: TimelineRepository,
    private val authRepository: AuthRepository, // Inject AuthRepository
    private val userRepository: UserRepository // Inject UserRepository
) : ViewModel() {

    // RepositoryのFlowを監視し、UIStateに変換して保持する
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TimelineUiState> = authRepository.isUserLoggedIn
        .map { isLoggedIn ->
            if (isLoggedIn) {
                val uid = authRepository.getCurrentUserId()
                if (uid != null) {
                    val user = userRepository.getUser(uid)
                    user?.groupId
                } else {
                    null
                }
            } else {
                null
            }
        }
        .flatMapLatest { groupId ->
            if (groupId != null) {
                repository.getTimelineStream(groupId) // Pass groupId to repository
                    .map { posts -> TimelineUiState.Success(posts) as TimelineUiState }
                    .catch { emit(TimelineUiState.Error(it.message ?: "Unknown error")) }
            } else {
                flowOf(TimelineUiState.Error("グループに所属していません。")) // Handle case where user is not in a group
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimelineUiState.Loading
        )

    fun vote(postId: String, voteType: VoteType) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: // エラー処理
            return@launch

            repository.votePost(postId, uid, voteType)
                .onFailure {
                    // エラー処理（本来はSnackbarなどで通知）
                    it.printStackTrace()
                }
        }
    }
}