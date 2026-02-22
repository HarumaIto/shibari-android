package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.betsudotai.shibari.presentation.ui.navigation.Screen

@Composable
fun MainScreen(parentNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    val bottomTabs = listOf(
        Screen.Quests,
        Screen.Timeline,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomTabs.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title!!) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Timeline.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Quests.route) { QuestsScreen(
                onNavigateToPost = { questId ->
                    parentNavController.navigate(Screen.Post.createRoute(questId))
                }
            ) }
            composable(Screen.Timeline.route) { TimelineScreen(
                onNavigateToComment = { postId ->
                    parentNavController.navigate(Screen.Comment.createRoute(postId))
                }
            ) }
            composable(Screen.Profile.route) { ProfileScreen(
                onNavigateToLogin = {
                    parentNavController.navigate(Screen.Auth.route)
                },
                onNavigateToEditQuests = {
                    parentNavController.navigate(Screen.QuestSelection.route)
                },
                onNavigateToProfileEdit = {
                    parentNavController.navigate(Screen.ProfileEdit.route)
                }
            ) }
        }
    }
}