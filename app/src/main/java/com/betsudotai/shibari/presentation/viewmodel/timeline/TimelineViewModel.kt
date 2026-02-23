package com.betsudotai.shibari.presentation.viewmodel.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.ReportRepository
import com.betsudotai.shibari.domain.repository.TimelineRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.domain.value.VoteType
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository ,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private var timelineJob: Job? = null

    init {
        loadTimeline()
    }

    fun loadTimeline() {
        timelineJob?.cancel()

        timelineJob = viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            val currentUser = userRepository.getUser(uid) ?: return@launch

            val groupId = currentUser.groupId ?: return@launch
            val blockedIds = currentUser.blockedUserIds

            timelineRepository.getTimelineStream(groupId)
                .catch { e ->
                    if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        _uiState.value = TimelineUiState.Loading
                    } else {
                        e.printStackTrace()
                    }
                }
                .collect { posts ->
                val filteredPosts = posts.filterNot { blockedIds.contains(it.userId) }
                _uiState.value = TimelineUiState.Success(filteredPosts, currentUser.uid)
            }
        }
    }

    fun vote(postId: String, voteType: VoteType) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: // エラー処理
            return@launch

            timelineRepository.votePost(postId, uid, voteType)
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    fun blockUser(targetUserId: String) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            userRepository.blockUser(uid, targetUserId)
            // 再読み込みしてタイムラインから消す
            loadTimeline()
        }
    }

    fun reportPost(targetUserId: String, postId: String, reason: String) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            reportRepository.reportContent(uid, targetUserId, postId, reason)
        }
    }
}