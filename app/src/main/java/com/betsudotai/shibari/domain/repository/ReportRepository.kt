package com.betsudotai.shibari.domain.repository

interface ReportRepository {
    suspend fun reportContent(reporterId: String, reportedUserId: String, postId: String, reason: String): Result<Unit>
}