package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.GroupRemoteDataSource
import com.betsudotai.shibari.data.dto.GroupDto
import com.betsudotai.shibari.domain.model.Group
import com.betsudotai.shibari.domain.repository.GroupRepository
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupRemoteDataSource: GroupRemoteDataSource
) : GroupRepository {

    override suspend fun createGroup(groupName: String, description: String, ownerId: String): Result<Group> {
        return runCatching {
            val newGroupDto = GroupDto(name = groupName, description = description, ownerId = ownerId, memberIds = listOf(ownerId))
            val createdGroupDto = groupRemoteDataSource.createGroup(newGroupDto)
            createdGroupDto.toDomain()
        }
    }

    override suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        return runCatching {
            groupRemoteDataSource.joinGroup(groupId, userId)
        }
    }

    override suspend fun getGroupDetails(groupId: String): Result<Group?> {
        return runCatching {
            val groupDto = groupRemoteDataSource.getGroupDetails(groupId)
            groupDto?.toDomain()
        }
    }

    override suspend fun getGroupByInvitationCode(invitationCode: String): Result<Group?> {
        return runCatching {
            val groupDto = groupRemoteDataSource.getGroupByInvitationCode(invitationCode)
            groupDto?.toDomain()
        }
    }
}

