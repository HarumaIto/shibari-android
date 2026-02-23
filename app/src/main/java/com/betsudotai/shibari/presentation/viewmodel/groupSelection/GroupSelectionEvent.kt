package com.betsudotai.shibari.presentation.viewmodel.groupSelection

sealed interface GroupSelectionEvent {
    data object NavigateToQuestSelection : GroupSelectionEvent
    data class ShowError(val message: String): GroupSelectionEvent
}
