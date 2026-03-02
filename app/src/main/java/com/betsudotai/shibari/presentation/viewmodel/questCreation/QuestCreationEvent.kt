package com.betsudotai.shibari.presentation.viewmodel.questCreation

sealed interface QuestCreationEvent {
    data object NavigateBack : QuestCreationEvent
    data class ShowError(val message: String) : QuestCreationEvent
}
