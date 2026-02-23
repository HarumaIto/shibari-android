package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.ReportDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReportRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ReportRemoteDataSource {

    override suspend fun createReport(report: ReportDto) {
        firestore.collection("reports")
            .document(report.id).set(report).await()
    }
}