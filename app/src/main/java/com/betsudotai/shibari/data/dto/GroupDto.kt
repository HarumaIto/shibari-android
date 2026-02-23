package com.betsudotai.shibari.data.dto

import com.betsudotai.shibari.domain.model.Group
import com.google.firebase.firestore.PropertyName

data class GroupDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("ownerId") val ownerId: String = "",
    @PropertyName("memberIds") val memberIds: List<String> = emptyList(),
    @PropertyName("invitationCode") val invitationCode: String = ""
) {
    fun toDomain(): Group {
        return Group(
            id = id,
            name = name,
            description = description,
            ownerId = ownerId,
            memberIds = memberIds,
            invitationCode = invitationCode
        )
    }

    fun toDto(): GroupDto {
        return GroupDto(
            id = id,
            name = name,
            description = description,
            ownerId = ownerId,
            memberIds = memberIds,
            invitationCode = invitationCode
        )
    }
}