package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.User
import java.io.File

interface UserRepository {
    // ユーザー情報の取得
    suspend fun getUser(userId: String): User?

    // 新規登録時のプロフィール作成
    suspend fun createUser(user: User): Result<Unit>

    // 参加するクエストの更新
    suspend fun updateParticipatingQuests(userId: String, questIds: List<String>): Result<Unit>

    suspend fun updateProfile(userId: String, displayName: String, photoFile: File?): Result<Unit>

    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
}