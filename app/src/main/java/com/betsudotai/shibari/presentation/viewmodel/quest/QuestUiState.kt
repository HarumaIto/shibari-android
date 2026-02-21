package com.betsudotai.shibari.presentation.viewmodel.quest

import com.betsudotai.shibari.domain.model.Quest

sealed interface QuestsUiState {
    data object Loading : QuestsUiState
    data class Success(val myQuests: List<Quest>) : QuestsUiState
    data class Error(val message: String) : QuestsUiState
}