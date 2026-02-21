package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.betsudotai.shibari.presentation.viewmodel.comment.CommentViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    viewModel: CommentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()

    val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("コメント") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        bottomBar = {
            // 画面下部の入力エリア
            BottomAppBar(
                modifier = Modifier.imePadding(), // キーボードが出た時に上に押し上げる設定
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = viewModel::onInputTextChanged,
                    placeholder = { Text("コメントを追加...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    enabled = !isSending
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.sendComment() },
                    enabled = inputText.isNotBlank() && !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "送信",
                            tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(comments) { comment ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = comment.author.photoUrl,
                        contentDescription = "Icon",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.author.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = comment.createdAt.format(formatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}