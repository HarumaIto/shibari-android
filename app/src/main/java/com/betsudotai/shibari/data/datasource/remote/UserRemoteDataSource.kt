package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.UserDto

interface UserRemoteDataSource {
    suspend fun getUser(userId: String): UserDto?
    suspend fun createUser(userDto: UserDto)
    suspend fun updateQuests(userId: String, questIds: List<String>)
}