package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.User
import com.google.firebase.firestore.PropertyName

data class UserDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("photoUrl") val photoUrl: String? = null,
    @PropertyName("fcmToken") val fcmToken: String? = null,
    @PropertyName("participatingQuestIds") val participatingQuestIds: List<String> = emptyList()
) {
    fun toDomain(): User {
        return User(
            uid = id,
            displayName = displayName,
            photoUrl = photoUrl,
            fcmToken = fcmToken,
            participatingQuestIds = participatingQuestIds
        )
    }
}