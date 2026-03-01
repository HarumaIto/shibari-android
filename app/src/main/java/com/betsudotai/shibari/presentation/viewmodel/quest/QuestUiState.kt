package com.betsudotai.shibari.presentation.viewmodel.quest

import com.betsudotai.shibari.domain.model.Quest
import com.betsudotai.shibari.domain.value.QuestFrequency

data class QuestGroup (
    val frequency: QuestFrequency,
    val quests: List<Quest>,
)

sealed interface QuestsUiState {
    data object Loading : QuestsUiState
    data class Success(val groupedQuests: List<QuestGroup>) : QuestsUiState
    data class Error(val message: String) : QuestsUiState
}