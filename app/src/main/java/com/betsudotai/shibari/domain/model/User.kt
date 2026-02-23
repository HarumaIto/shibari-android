package com.betsudotai.shibari.domain.model

data class User(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val fcmToken: String?,
    val participatingQuestIds: List<String>,
    val groupId: String?,
    val blockedUserIds: List<String>
)