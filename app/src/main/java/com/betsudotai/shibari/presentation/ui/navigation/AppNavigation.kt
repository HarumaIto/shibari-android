package com.betsudotai.shibari.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.betsudotai.shibari.presentation.ui.screens.AuthScreen
import com.betsudotai.shibari.presentation.ui.screens.CommentScreen
import com.betsudotai.shibari.presentation.ui.screens.MainScreen
import com.betsudotai.shibari.presentation.ui.screens.PostScreen
import com.betsudotai.shibari.presentation.ui.screens.ProfileEditScreen
import com.betsudotai.shibari.presentation.ui.screens.ProfileSetupScreen
import com.betsudotai.shibari.presentation.ui.screens.QuestSelectionScreen
import com.betsudotai.shibari.presentation.ui.screens.GroupSelectionScreen

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
                onNavigateToGroupSelection = {
                    navController.navigate(Screen.GroupSelection.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = { navController.popBackStack() }
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

        composable(Screen.GroupSelection.route) {
            GroupSelectionScreen(
                onNavigateToQuestSelection = {
                    navController.navigate(Screen.QuestSelection.route) {
                        popUpTo(Screen.GroupSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(navController)
        }

        composable(
            route = Screen.Post.route,
            arguments = listOf(navArgument("questId") { type = NavType.StringType })
        ) {
            PostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Comment.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            CommentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}