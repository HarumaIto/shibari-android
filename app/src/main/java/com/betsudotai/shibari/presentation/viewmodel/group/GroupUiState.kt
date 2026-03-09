package com.betsudotai.shibari.presentation.viewmodel.group

import com.betsudotai.shibari.domain.model.Group
import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.model.User

sealed class GroupUiState {
    data object Loading : GroupUiState()
    data class Success(val group: Group, val members: List<User>, val questMap: Map<String, Quest>) : GroupUiState()
    data class Error(val message: String) : GroupUiState()
}
