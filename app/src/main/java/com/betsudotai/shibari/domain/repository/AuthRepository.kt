package com.betsudotai.shibari.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // 現在のログイン状態とユーザーIDを監視するFlow
    val userIdFlow: Flow<String?>

    fun getCurrentUserId(): String?

    suspend fun getFCMToken(): String?

    // メールリンク or メールパスワード等でのサインイン（今回はシンプルなメール＆パスワードを想定）
    suspend fun signIn(email: String, password: String): Result<String?>
    suspend fun signUp(email: String, password: String): Result<String> // 成功時にUIDを返す

    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}