package com.betsudotai.shibari.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class ReportDto (
    @DocumentId val documentId: String = "",
    @PropertyName("reporterId") val reporterId: String = "",
    @PropertyName("reportedUserId") val reportedUserId: String = "",
    @PropertyName("postId") val postId: String = "",
    @PropertyName("reason") val reason: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp? = null
)