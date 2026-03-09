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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.betsudotai.shibari.presentation.viewmodel.group.GroupUiState
import com.betsudotai.shibari.presentation.viewmodel.group.GroupViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.tooling.preview.Preview
import com.betsudotai.shibari.domain.model.Group
import com.betsudotai.shibari.domain.model.User
import com.betsudotai.shibari.presentation.ui.theme.ShibariTheme
import kotlinx.coroutines.CoroutineScope

@Composable
fun GroupScreen(
    viewModel: GroupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToGroupQuests: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    GroupScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        clipboardManager = clipboardManager,
        scope = scope,
        onNavigateBack = onNavigateBack,
        onNavigateToGroupQuests = onNavigateToGroupQuests,
        loadGroupData = viewModel::loadGroupData,
        getQuestTitles = viewModel::getQuestTitles
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreenContent(
    uiState: GroupUiState,
    snackbarHostState: SnackbarHostState,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    scope: CoroutineScope,
    onNavigateBack: () -> Unit,
    onNavigateToGroupQuests: () -> Unit,
    loadGroupData: () -> Unit,
    getQuestTitles: (member: User) -> String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("グループ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (uiState) {
            is GroupUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is GroupUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadGroupData() }) {
                            Text("再読み込み")
                        }
                    }
                }
            }

            is GroupUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ヘッダー: グループ名と説明
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.group.name,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.group.description,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // グループの縛り一覧ボタン
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToGroupQuests() }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "グループの縛り一覧",
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // 招待コードカード
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "招待コード",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.group.invitationCode,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(uiState.group.invitationCode))
                                            scope.launch {
                                                snackbarHostState.showSnackbar("招待コードをコピーしました！")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "コピー"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // メンバー一覧ヘッダー
                    item {
                        Text(
                            text = "メンバー",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // メンバーリスト
                    items(uiState.members.size) { index ->
                        val member = uiState.members[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (member.photoUrl != null) {
                                    AsyncImage(
                                        model = member.photoUrl,
                                        contentDescription = member.displayName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = member.displayName.take(1).uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column{
                                Text(
                                    text = member.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "クエストタイトル",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = getQuestTitles(member),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (member.uid == uiState.group.ownerId) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "オーナー",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupScreenContentPreview() {
    ShibariTheme (
        darkTheme = true,
    ) {
        GroupScreenContent(
            uiState = GroupUiState.Success(
                group = Group(),
                members = listOf(
                    User(
                        uid = "test",
                        displayName = "テストユーザー",
                        photoUrl = null,
                        fcmToken = null,
                        participatingQuestIds = listOf(
                            "mock_quest_id_1",
                            "mock_quest_id_2"
                        ),
                        groupId = "",
                        blockedUserIds = listOf()
                    )
                ),
                questMap = mapOf()
            ),
            snackbarHostState = remember { SnackbarHostState() },
            clipboardManager = LocalClipboardManager.current,
            scope = rememberCoroutineScope(),
            onNavigateBack = {},
            onNavigateToGroupQuests = {},
            loadGroupData = {},
            getQuestTitles = { user: User -> ""}
        )
    }
}