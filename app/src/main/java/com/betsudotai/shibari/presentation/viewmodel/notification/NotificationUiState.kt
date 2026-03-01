package com.betsudotai.shibari.presentation.viewmodel.notification

import com.betsudotai.shibari.domain.model.AppNotification

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Success(val notifications: List<AppNotification>) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}
