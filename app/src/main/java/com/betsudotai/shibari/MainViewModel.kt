package com.betsudotai.shibari

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.betsudotai.shibari.presentation.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val startDestination: StateFlow<String?> = authRepository.userIdFlow
        .map { userId ->
            if (userId != null) {
                val user = userRepository.getUser(userId)
                if (user == null) {
                    Screen.ProfileSetup.route
                } else if (user.groupId == null) {
                    Screen.GroupSelection.route
                } else {
                    Screen.Main.route
                }
            } else {
                Screen.Auth.route
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

