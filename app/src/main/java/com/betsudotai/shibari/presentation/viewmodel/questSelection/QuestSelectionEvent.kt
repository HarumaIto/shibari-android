package com.betsudotai.shibari.presentation.viewmodel.questSelection

import com.betsudotai.shibari.domain.repository.QuestRepository

sealed interface QuestSelectionEvent {
    data object NavigateToMain: QuestSelectionEvent
    data class ShowError(val message: String): QuestSelectionEvent
}