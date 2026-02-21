package com.betsudotai.shibari.presentation.viewmodel.post

sealed interface PostEvent {
    data object NavigateBack: PostEvent
    data class ShowError(val message: String): PostEvent
}