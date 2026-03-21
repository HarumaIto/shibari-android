package com.betsudotai.shibari.domain.model.timeline

import com.betsudotai.shibari.domain.value.JudgmentResult
import java.util.Date

data class AiJudgment(
    val judgedAt: Date,
    val reason: String,
    val result: JudgmentResult
)