package com.betsudotai.shibari.presentation.viewmodel.timeline

import com.betsudotai.shibari.domain.model.TimelinePost

// UIの状態を表すクラス
sealed interface TimelineUiState {
    data object Loading : TimelineUiState
    data class Success(val posts: List<TimelinePost>, val userId: String) : TimelineUiState
    data class Error(val message: String) : TimelineUiState
}