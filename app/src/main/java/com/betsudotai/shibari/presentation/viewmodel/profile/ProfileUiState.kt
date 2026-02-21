package com.betsudotai.shibari.presentation.viewmodel.profile

import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.model.User

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: User, val participatingQuests: List<Quest>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}