package com.betsudotai.shibari.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.betsudotai.shibari.presentation.ui.screens.AuthScreen
import com.betsudotai.shibari.presentation.ui.screens.ProfileSetupScreen
import com.betsudotai.shibari.presentation.ui.screens.TimelineScreen

@Composable
fun AppNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ① 認証画面 (AuthScreen)
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToTimeline = {
                    // タイムラインへ移動し、戻るボタンでAuth画面に戻らないように履歴を消す
                    navController.navigate(Screen.Timeline.route) {
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

        // ② プロフィール設定画面 (ProfileSetupScreen)
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onNavigateToTimeline = {
                    navController.navigate(Screen.Timeline.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // ③ タイムライン画面 (TimelineScreen)
        composable(Screen.Timeline.route) {
            TimelineScreen()
        }
    }
}