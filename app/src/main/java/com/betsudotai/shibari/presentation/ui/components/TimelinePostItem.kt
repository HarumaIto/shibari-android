package com.betsudotai.shibari.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.value.MediaType
import com.betsudotai.shibari.domain.value.VoteType
import java.time.format.DateTimeFormatter
import com.betsudotai.shibari.presentation.ui.components.AiJudgmentDisplay

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
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Header: ユーザー情報 & クエスト名 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),

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
                    post.createdAt?.let { date ->
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            Column {
                if (post.mediaType == MediaType.VIDEO) {
                    VideoPlayer(
                        videoUri = post.mediaUrl!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                    )
                } else {
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = "Evidence",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black),
                        contentScale = ContentScale.Fit
                    )
                }

                if (post.comment != null && post.comment.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // --- AI Judgment Area ---
            post.aiJudgment?.let { aiJudgment ->
                AiJudgmentDisplay(aiJudgment = aiJudgment)
            }

            // --- Footer: コメント & アクション ---
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                if (post.latestComments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        post.latestComments.forEach { commentText ->
                            Text(
                                text = commentText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val previewText = when {
                    post.commentCount > post.latestComments.size -> "コメント ${post.commentCount} 件をすべて見る"
                    post.commentCount == 0 -> "最初のコメントを書く..."
                    else -> null
                }

                if (previewText != null) {
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable { onCommentClick(post.id) }
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onCommentClick(post.id) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "コメント")
                        if (post.commentCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.commentCount.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 否認ボタン（iOSのタクティカルレッド相当）
                    OutlinedButton(
                        onClick = { onVote(post.id, VoteType.REJECT) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ThumbDown, contentDescription = "Reject")
                        Spacer(modifier = Modifier.width(4.dp))
                        // ★ 新しく追加した rejectCount を表示
                        Text("否認 (${post.rejectCount})")
                    }

                    Spacer(modifier = Modifier.width(width = 8.dp))

                    // 承認ボタン（iOSのネオングリーン相当）
                    OutlinedButton(
                        onClick = { onVote(post.id, VoteType.APPROVE) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = androidx.compose.ui.graphics.Color(0xFF00C853) // ネオングリーン
                        ),
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF00C853).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = "Approve")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("承認 (${post.approvalCount})")
                    }
                }
            }
        }
    }
}