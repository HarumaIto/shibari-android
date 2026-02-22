package com.betsudotai.shibari.data.repository

import com.betsudotai.shibari.domain.model.User
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging
) : AuthRepository {

    override val isUserLoggedIn: Flow<Boolean> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun getFCMToken(): String? {
        return firebaseMessaging.token.await()
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return runCatching {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Unit
        }
    }

    override suspend fun signUp(email: String, password: String): Result<String> {
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid ?: throw Exception("User creation failed")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return runCatching {
            val user = firebaseAuth.currentUser ?: throw Exception("Not logged in")
            user.delete().await()
        }
    }
}