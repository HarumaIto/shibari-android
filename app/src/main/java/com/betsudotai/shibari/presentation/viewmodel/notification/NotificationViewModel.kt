package com.betsudotai.shibari.presentation.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                _uiState.value = NotificationUiState.Error("ユーザーが見つかりません")
                return@launch
            }

            notificationRepository.getNotifications(userId)
                .onSuccess { notifications ->
                    // Display notifications with their original isRead values so unread indicators
                    // are visible while the page is open.
                    _uiState.value = NotificationUiState.Success(notifications)

                    // Immediately mark all unread notifications as read in Firestore in the background.
                    val unreadIds = notifications.filter { !it.isRead }.map { it.id }
                    if (unreadIds.isNotEmpty()) {
                        notificationRepository.markAllAsRead(userId, unreadIds)
                            .onFailure { it.printStackTrace() }
                    }
                }
                .onFailure { e ->
                    _uiState.value = NotificationUiState.Error(e.message ?: "エラーが発生しました")
                }
        }
    }
}
