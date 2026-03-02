package com.betsudotai.shibari.domain.usecase

import com.betsudotai.shibari.domain.model.Quest

interface GetMyQuestsUseCase {
    suspend operator fun invoke(userId: String, groupId: String): List<Quest>
}
