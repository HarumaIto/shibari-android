package com.betsudotai.shibari.domain.model

import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType

data class Quest(
    val id: String,
    val groupId: String,
    val title: String,
    val type: QuestType,
    val frequency: QuestFrequency,
    val description: String,
    val threshold: Int?,
    val isCompleted: Boolean = false
)