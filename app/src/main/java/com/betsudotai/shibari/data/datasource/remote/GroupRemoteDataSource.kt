package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.GroupDto

interface GroupRemoteDataSource {
    suspend fun createGroup(groupDto: GroupDto): GroupDto
    suspend fun joinGroup(groupId: String, userId: String)
    suspend fun getGroupDetails(groupId: String): GroupDto?
    suspend fun getGroupByInvitationCode(invitationCode: String): GroupDto?
}
