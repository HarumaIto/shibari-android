package com.betsudotai.shibari.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.value.MediaType
import com.betsudotai.shibari.domain.value.VoteType

@Composable
fun TimelinePostItem(
    post: TimelinePost,
    currentUserId: String,
    onVote: (String, VoteType) -> Unit,
    onCommentClick: (String) -> Unit,
    onBlockClick: (String) -> Unit,
    onReportClick: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // --- Header: ユーザー情報 & クエスト名 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {
                // アイコン（なければグレーの円）
                AsyncImage(
                    model = post.author.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.author.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "縛り: ${post.quest.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // ステータスバッジ
                Text(
                    text = post.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                if (post.userId != currentUserId) {
                    Box {
                        IconButton(onClick = { expanded = true}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("不適切なコンテンツを通報") },
                                onClick = {
                                    expanded = false
                                    onReportClick(post.userId, post.id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("このユーザーをブロック", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    expanded = false
                                    onBlockClick(post.userId)
                                }
                            )
                        }
                    }
                }
            }

            // --- Media: メイン画像 ---
            if (post.mediaType == MediaType.VIDEO) {
                VideoPlayer(
                    videoUri = post.mediaUrl!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // 正方形
                        .background(Color.Black)
                )
            } else {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Evidence",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black),
                    contentScale = ContentScale.Crop
                )
            }

            // --- Footer: コメント & アクション ---
            Column(modifier = Modifier.padding(12.dp)) {
                if (post.comment != null && post.comment.isNotEmpty()) {
                    Text(
                        text = post.comment,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ★追加：コメントアイコンボタン
                    IconButton(onClick = { onCommentClick(post.id) }) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "コメント")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "承認: ${post.approvalCount}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        IconButton(onClick = { onVote(post.id, VoteType.REJECT) }) {
                            Icon(Icons.Default.ThumbDown, contentDescription = "Reject", tint = Color.Gray)
                        }
                        FilledTonalButton(onClick = { onVote(post.id, VoteType.APPROVE) }) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("承認")
                        }
                    }
                }
            }
        }
    }
}