package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.betsudotai.shibari.presentation.ui.components.common.KeyValueCard
import com.betsudotai.shibari.presentation.viewmodel.profile.ProfileUiState
import com.betsudotai.shibari.presentation.viewmodel.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToEditQuests: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "プロフィール",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                    ) },
                actions = {
                    IconButton(onClick = onNavigateToProfileEdit) {
                        Icon(Icons.Default.List, contentDescription = "プロフィール編集")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProfileUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "エラー: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile() }) { Text("再読み込み") }
                    }
                }
                is ProfileUiState.Success -> {
                    val user = state.user
                    val myQuests = state.participatingQuests

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- 1. ユーザー情報セクション ---
                        item {
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Profile Icon",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                KeyValueCard(
                                    modifier = Modifier.weight(1f),
                                    key = "承認数",
                                    value = "142"
                                )
                                KeyValueCard(
                                    modifier = Modifier.weight(1f),
                                    key = "現在の連続日数",
                                    value = "12日"
                                )
                            }
                            Spacer(modifier = Modifier.height(100.dp))
                        }

                        // --- 2. 参加中の縛りセクション ---
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "参加中の縛り",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                // 編集ボタン
                                IconButton(onClick = onNavigateToEditQuests) {
                                    Icon(Icons.Default.Edit, contentDescription = "編集する", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        if (myQuests.isEmpty()) {
                            item {
                                Text(
                                    text = "現在参加している縛りはありません。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(myQuests.size) { index ->
                                val quest = myQuests[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔥", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(quest.title, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(48.dp)) }

                        // --- 3. ログアウトボタン ---
                        item {
                            OutlinedButton(
                                onClick = { viewModel.signOut() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Text("ログアウト")
                            }
                        }
                    }
                }
            }
        }
    }
}