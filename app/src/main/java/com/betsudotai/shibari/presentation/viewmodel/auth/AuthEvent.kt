package com.betsudotai.shibari.presentation.viewmodel.auth

sealed interface AuthEvent {
    data object NavigateToTimeline: AuthEvent
    data object NavigateToProfileSetup: AuthEvent
    data class ShowError(val message: String): AuthEvent
}