package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.Group
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class GroupDto(
    @DocumentId val documentId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("ownerId") val ownerId: String = "",
    @PropertyName("memberIds") val memberIds: List<String> = emptyList(),
    @PropertyName("invitationCode") val invitationCode: String = ""
) {
    fun toDomain(): Group {
        return Group(
            id = documentId,
            name = name,
            description = description,
            ownerId = ownerId,
            memberIds = memberIds,
            invitationCode = invitationCode
        )
    }
}