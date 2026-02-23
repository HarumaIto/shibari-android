package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betsudotai.shibari.presentation.ui.components.TimelinePostItem
import com.betsudotai.shibari.presentation.viewmodel.timeline.TimelineUiState
import com.betsudotai.shibari.presentation.viewmodel.timeline.TimelineViewModel

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onNavigateToComment: (String) -> Unit
) {
    // ViewModelの状態を監視
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var reportingPostId by remember { mutableStateOf<String?>(null) }
    var reportingUserId by remember { mutableStateOf<String?>(null) }
    var reportReason by remember { mutableStateOf("") }

    if (reportingPostId != null && reportingUserId != null) {
        AlertDialog(
            onDismissRequest = {
                reportingPostId = null
                reportingUserId = null
                reportReason = ""
            },
            title = { Text("不適切なコンテンツを通報") },
            text = {
                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    label = { Text("通報の理由（必須）") },
                    placeholder = { Text("例: 暴言が含まれている、等") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // ViewModelの通報処理を呼ぶ
                        viewModel.reportPost(
                            reportingUserId!!,
                            reportingPostId!!,
                            reportReason
                        )
                        // ダイアログを閉じてリセット
                        reportingPostId = null
                        reportingUserId = null
                        reportReason = ""
                    },
                    enabled = reportReason.isNotBlank() // 理由が空なら押せない
                ) {
                    Text("通報する")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    reportingPostId = null
                    reportingUserId = null
                    reportReason = ""
                }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is TimelineUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is TimelineUiState.Error -> {
                Text(
                    text = "エラー: ${state.message}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is TimelineUiState.Success -> {
                if (state.posts.isEmpty()) {
                    Text(
                        text = "まだ投稿がありません",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.posts.size) { index ->
                            val post = state.posts[index]
                            TimelinePostItem(
                                post = post,
                                currentUserId = state.userId,
                                onVote = { id, type -> viewModel.vote(id, type) },
                                onCommentClick = { id ->
                                    onNavigateToComment(id)
                                },
                                onBlockClick = { userId ->
                                    viewModel.blockUser(userId)
                                },
                                onReportClick = { userId, postId ->
                                    reportingUserId = userId
                                    reportingPostId = postId
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}