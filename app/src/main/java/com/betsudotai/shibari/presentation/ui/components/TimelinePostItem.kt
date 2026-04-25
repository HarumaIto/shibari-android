package com.betsudotai.shibari.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.domain.value.MediaType
import com.betsudotai.shibari.domain.value.VoteType
import com.betsudotai.shibari.presentation.ui.theme.TimelineDivider
import com.betsudotai.shibari.presentation.ui.theme.VoteApproveColor
import com.betsudotai.shibari.presentation.ui.theme.VoteRejectColor
import java.time.format.DateTimeFormatter

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

    val myVote = post.votes[currentUserId]
    val isApproveActive = myVote == VoteType.APPROVE
    val isRejectActive = myVote == VoteType.REJECT

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 7.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Header: ユーザー情報 & ステータスバッジ ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "縛り: ${post.quest.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = post.status)
                if (post.userId != currentUserId) {
                    Box {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "メニュー",
                                modifier = Modifier.size(18.dp),
                            )
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
                                text = {
                                    Text(
                                        "このユーザーをブロック",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
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
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                val mediaShape = RoundedCornerShape(12.dp)
                if (post.mediaType == MediaType.VIDEO) {
                    VideoPlayer(
                        videoUri = post.mediaUrl!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(mediaShape)
                            .background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = "Evidence",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(mediaShape)
                            .background(MaterialTheme.colorScheme.background),
                        contentScale = ContentScale.Fit
                    )
                }

                if (post.comment != null && post.comment.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.comment,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // 投稿日時を画像下に控えめに配置
                post.createdAt?.let { date ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // --- AI Judgment Area ---
            post.aiJudgment?.let { aiJudgment ->
                AiJudgmentDisplay(aiJudgment = aiJudgment)
            }

            // --- Footer: コメント & アクション ---
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // コメントボタン（アイコンのみのピル）
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, TimelineDivider, RoundedCornerShape(20.dp))
                            .clickable { onCommentClick(post.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = "コメント",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (post.commentCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = post.commentCount.toString(),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 否認ボタン（✗アイコン+カウント）
                    VotePill(
                        icon = Icons.Default.Close,
                        contentDescription = "否認",
                        count = post.rejectCount,
                        active = isRejectActive,
                        color = VoteRejectColor,
                        onClick = { onVote(post.id, VoteType.REJECT) },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 承認ボタン（✓アイコン+カウント）
                    VotePill(
                        icon = Icons.Default.Check,
                        contentDescription = "承認",
                        count = post.approvalCount,
                        active = isApproveActive,
                        color = VoteApproveColor,
                        onClick = { onVote(post.id, VoteType.APPROVE) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VotePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    count: Int,
    active: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    val border = if (active) color else TimelineDivider
    val bg = if (active) color.copy(alpha = 0.13f) else Color.Transparent
    val iconTint = if (active) color else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (active) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = iconTint,
        )
        Text(
            text = count.toString(),
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}
