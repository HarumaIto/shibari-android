package com.betsudotai.shibari.presentation.viewmodel.profileSetup

sealed interface ProfileSetupEvent {
    data object NavigateToTimeline : ProfileSetupEvent
    data class ShowError(val message: String) : ProfileSetupEvent
}