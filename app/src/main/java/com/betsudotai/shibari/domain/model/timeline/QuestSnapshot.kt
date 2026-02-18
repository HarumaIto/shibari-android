package com.betsudotai.shibari.domain.model.timeline

import com.betsudotai.shibari.domain.value.QuestType

data class QuestSnapshot(
    val title: String,
    val type: QuestType
)
