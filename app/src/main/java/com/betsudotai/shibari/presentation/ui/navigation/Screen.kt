package com.betsudotai.shibari.presentation.ui.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object ProfileSetup : Screen("profile_setup")
    data object Timeline : Screen("timeline")
}