package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.User
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserDto(
    @DocumentId val documentId: String = "",
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("photoUrl") val photoUrl: String? = null,
    @PropertyName("fcmToken") val fcmToken: String? = null,
    @PropertyName("participatingQuestIds") val participatingQuestIds: List<String> = emptyList(),
    @PropertyName("groupId") val groupId: String? = null,
    @PropertyName("blockedUserIds") val blockedUserIds: List<String> = emptyList()
) {
    fun toDomain(): User {
        return User(
            uid = documentId,
            displayName = displayName,
            photoUrl = photoUrl,
            fcmToken = fcmToken,
            participatingQuestIds = participatingQuestIds,
            groupId = groupId,
            blockedUserIds = blockedUserIds
        )
    }
}