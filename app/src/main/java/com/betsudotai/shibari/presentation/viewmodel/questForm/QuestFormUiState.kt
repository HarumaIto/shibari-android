package com.betsudotai.shibari.presentation.viewmodel.questForm

import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType

data class QuestFormUiState(
    val title: String = "",
    val description: String = "",
    val type: QuestType = QuestType.ROUTINE,
    val frequency: QuestFrequency = QuestFrequency.DAILY,
    val thresholdText: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean get() = title.isNotBlank() && description.isNotBlank()
}
