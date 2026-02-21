package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.UserDto
import java.io.File

interface UserRemoteDataSource {
    suspend fun getUser(userId: String): UserDto?
    suspend fun createUser(userDto: UserDto)
    suspend fun updateQuests(userId: String, questIds: List<String>)
    suspend fun updateProfile(userId: String, displayName: String, photoUrl: String?)
    suspend fun uploadProfileImage(userId: String, file: File): String
}