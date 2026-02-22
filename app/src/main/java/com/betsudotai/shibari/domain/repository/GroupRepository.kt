package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.Group

interface GroupRepository {
    suspend fun createGroup(groupName: String, description: String, ownerId: String): Result<Group>
    suspend fun joinGroup(groupId: String, userId: String): Result<Unit>
    suspend fun getGroupDetails(groupId: String): Result<Group?>
    suspend fun getGroupByInvitationCode(invitationCode: String): Result<Group?>
}
