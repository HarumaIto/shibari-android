package com.betsudotai.shibari.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    data object Auth : Screen("auth")
    data object ProfileSetup : Screen("profile_setup")
    data object QuestSelection : Screen("quest_selection")
    data object GroupSelection: Screen("group_selection")

    data object ProfileEdit: Screen("profile_edit")

    data object Main : Screen("main")

    data object Post: Screen("post/{questId}") {
        fun createRoute(questId: String) = "post/$questId"
    }

    data object Comment: Screen("comment/{postId}") {
        fun createRoute(postId: String) = "comment/$postId"
    }

    data object Notifications: Screen("notifications")

    data object Quests: Screen("quests", "クエスト", Icons.Default.List)
    data object Timeline: Screen("timeline", "タイムライン", Icons.Default.Home)
    data object  Profile: Screen("profile", "プロフィール", Icons.Default.Person)
}