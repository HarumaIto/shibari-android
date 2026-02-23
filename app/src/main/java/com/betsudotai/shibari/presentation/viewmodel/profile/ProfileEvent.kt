package com.betsudotai.shibari.presentation.viewmodel.profile

sealed interface ProfileEvent {
    data object NavigateToLogin: ProfileEvent
    data class ShowError(val message: String): ProfileEvent
}