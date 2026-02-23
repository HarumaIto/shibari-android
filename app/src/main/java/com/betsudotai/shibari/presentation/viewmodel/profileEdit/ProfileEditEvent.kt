package com.betsudotai.shibari.presentation.viewmodel.profileEdit

sealed interface ProfileEditEvent {
    data object NavigateBack : ProfileEditEvent
    data class ShowError(val message: String) : ProfileEditEvent
}