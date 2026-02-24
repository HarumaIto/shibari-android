package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.data.datasource.remote.UserRemoteDataSource
import com.betsudotai.shibari.data.dto.ReportDto
import com.betsudotai.shibari.data.dto.UserDto
import com.betsudotai.shibari.domain.model.User
import com.betsudotai.shibari.domain.repository.UserRepository
import com.google.firebase.Timestamp
import java.io.File
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun getUser(userId: String): User? {
        return runCatching {
            remoteDataSource.getUser(userId)?.toDomain()
        }.getOrNull()
    }

    override suspend fun createUser(user: User): Result<Unit> {
        return runCatching {
            val dto = UserDto(
                documentId = user.uid,
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                fcmToken = user.fcmToken,
                participatingQuestIds = user.participatingQuestIds
            )
            remoteDataSource.createUser(dto)
        }
    }

    override suspend fun updateParticipatingQuests(userId: String, questIds: List<String>): Result<Unit> {
        return runCatching {
            remoteDataSource.updateQuests(userId, questIds)
        }
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        photoFile: File?
    ): Result<Unit> {
        return runCatching {
            val photoUrl = if (photoFile != null) {
                remoteDataSource.uploadProfileImage(userId, photoFile)
            } else {
                null
            }
            remoteDataSource.updateProfile(userId, displayName, photoUrl)
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return runCatching {
            remoteDataSource.updateFcmToken(userId, token)
        }
    }

    override suspend fun updateUserGroupId(userId: String, groupId: String?): Result<Unit> {
        return runCatching {
            remoteDataSource.updateUserGroupId(userId, groupId)
        }
    }

    override suspend fun anonymizeUser(userId: String): Result<Unit> {
        return runCatching {
            val updates = mapOf<String, Any?>(
                "displayName" to "退会済みユーザー",
                "photoUrl" to null,
                "fcmToken" to null
            )
            remoteDataSource.updateUser(userId, updates)
        }
    }

    override suspend fun blockUser(currentUserId: String, targetUserId: String): Result<Unit> {
        return runCatching {
            remoteDataSource.blockUser(currentUserId, targetUserId)
        }
    }
}