package com.betsudotai.shibari.presentation.viewmodel.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.R
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // UI側で監視するイベント
    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow: SharedFlow<AuthEvent> = _eventFlow.asSharedFlow()

    fun onEmailChange(newEmail: String) { _email.update { newEmail } }
    fun onPasswordChange(newPassword: String) { _password.update { newPassword } }

    fun signUp() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUp(email.value, password.value)

            result.onSuccess {
                // 新規登録成功時はプロフィール設定へ
                _eventFlow.emit(AuthEvent.NavigateToProfileSetup)
            }.onFailure { e ->
                _eventFlow.emit(AuthEvent.ShowError(e.message ?: "登録に失敗しました"))
            }
            _isLoading.value = false
        }
    }

    fun signIn() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signIn(email.value, password.value)

            result.onSuccess { uid ->
                if (uid == null) {
                    _eventFlow.emit(AuthEvent.ShowError("ログインに失敗しました: ユーザーIDが取得できませんでした。"))
                    _isLoading.value = false
                    return@onSuccess
                }

                // ログイン成功時、Firestoreにユーザーデータがあるかチェック
                val userProfile = userRepository.getUser(uid)
                if (userProfile != null) {
                    _eventFlow.emit(AuthEvent.NavigateToMain)
                } else {
                    _eventFlow.emit(AuthEvent.NavigateToProfileSetup)
                }
            }.onFailure { e ->
                _eventFlow.emit(AuthEvent.ShowError(e.message ?: "ログインに失敗しました"))
            }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val credentialManager = CredentialManager.create(context)
                val webClientId =
                    context.getString(R.string.default_web_client_id)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(context, request)

                val credential = response.credential
                if (credential !is CustomCredential || credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    _eventFlow.emit(AuthEvent.ShowError("サポートされていない認証方式です"))
                    _isLoading.value = false
                    return@launch
                }

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val result = authRepository.signInWithGoogle(googleIdTokenCredential.idToken)

                result.onSuccess { uid ->
                    if (uid == null) {
                        _eventFlow.emit(AuthEvent.ShowError("Googleログインに失敗しました: ユーザーIDが取得できませんでした。"))
                        _isLoading.value = false
                        return@onSuccess
                    }

                    val userProfile = userRepository.getUser(uid)
                    if (userProfile != null) {
                        _eventFlow.emit(AuthEvent.NavigateToMain)
                    } else {
                        _eventFlow.emit(AuthEvent.NavigateToProfileSetup)
                    }
                }
            } catch (e: Exception) {
                _eventFlow.emit(AuthEvent.ShowError(e.message ?: "ログインに失敗しました"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}