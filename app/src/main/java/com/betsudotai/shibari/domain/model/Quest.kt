package com.betsudotai.shibari.domain.model

import com.betsudotai.shibari.domain.value.QuestType

data class Quest(
    val id: String,
    val title: String,
    val type: QuestType,
    val description: String,
    val threshold: Int?
)