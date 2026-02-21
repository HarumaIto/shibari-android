package com.betsudotai.shibari.domain.repository

import com.betsudotai.shibari.domain.model.User

interface UserRepository {
    // ユーザー情報の取得
    suspend fun getUser(userId: String): User?

    // 新規登録時のプロフィール作成
    suspend fun createUser(user: User): Result<Unit>

    // 参加するクエストの更新
    suspend fun updateParticipatingQuests(userId: String, questIds: List<String>): Result<Unit>
}