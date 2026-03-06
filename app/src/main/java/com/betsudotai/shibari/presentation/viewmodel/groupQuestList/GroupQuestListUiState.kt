package com.betsudotai.shibari.presentation.viewmodel.groupQuestList

import com.betsudotai.shibari.domain.model.Quest

data class GroupQuestListUiState(
    val quests: List<Quest> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
