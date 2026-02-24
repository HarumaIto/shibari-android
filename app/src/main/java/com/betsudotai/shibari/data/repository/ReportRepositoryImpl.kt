package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.ReportRemoteDataSource
import com.betsudotai.shibari.data.dto.ReportDto
import com.betsudotai.shibari.domain.repository.ReportRepository
import com.google.firebase.Timestamp
import java.util.UUID
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReportRemoteDataSource
) : ReportRepository {
    override suspend fun reportContent(reporterId: String, reportedUserId: String, postId: String, reason: String): Result<Unit> {
        return runCatching {
            val reportId = UUID.randomUUID().toString()
            val report = ReportDto(
                documentId = reportId,
                reporterId = reporterId,
                reportedUserId = reportedUserId,
                postId = postId,
                reason = reason,
                createdAt = Timestamp.now()
            )
            remoteDataSource.createReport(report)
        }
    }
}