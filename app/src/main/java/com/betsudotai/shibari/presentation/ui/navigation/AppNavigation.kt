package com.betsudotai.shibari.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.betsudotai.shibari.presentation.ui.screens.AuthScreen
import com.betsudotai.shibari.presentation.ui.screens.MainScreen
import com.betsudotai.shibari.presentation.ui.screens.ProfileSetupScreen
import com.betsudotai.shibari.presentation.ui.screens.QuestSelectionScreen
import com.betsudotai.shibari.presentation.ui.screens.TimelineScreen

@Composable
fun AppNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(startDestination) {
        if (startDestination == Screen.Auth.route && navController.currentDestination?.route != Screen.Auth.route) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToMain = {
                    // メインへ移動し、戻るボタンでAuth画面に戻らないように履歴を消す
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    // プロフィール設定へ移動
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.QuestSelection.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QuestSelection.route) {
            QuestSelectionScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.QuestSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }

        composable(Screen.Timeline.route) {
            TimelineScreen()
        }
    }
}