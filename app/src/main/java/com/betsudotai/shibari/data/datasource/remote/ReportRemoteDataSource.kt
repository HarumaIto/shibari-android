package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.ReportDto

interface ReportRemoteDataSource {
    suspend fun createReport(report: ReportDto)
}