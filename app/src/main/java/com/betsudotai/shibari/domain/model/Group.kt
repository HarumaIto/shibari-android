package com.betsudotai.shibari.domain.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val invitationCode: String = ""
)
