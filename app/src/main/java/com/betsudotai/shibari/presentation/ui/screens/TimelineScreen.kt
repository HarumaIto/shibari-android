package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
                            TimelinePostItem(
                                post = state.posts[index],
                                onVote = { id, type -> viewModel.vote(id, type) },
                                onCommentClick = { id ->
                                    onNavigateToComment(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}