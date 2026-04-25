package com.betsudotai.shibari.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betsudotai.shibari.domain.value.PostStatus
import com.betsudotai.shibari.presentation.ui.theme.StatusApprovedBg
import com.betsudotai.shibari.presentation.ui.theme.StatusApprovedFg
import com.betsudotai.shibari.presentation.ui.theme.StatusPendingBg
import com.betsudotai.shibari.presentation.ui.theme.StatusPendingFg
import com.betsudotai.shibari.presentation.ui.theme.StatusRejectedBg
import com.betsudotai.shibari.presentation.ui.theme.StatusRejectedFg
import com.betsudotai.shibari.presentation.ui.theme.TimelineSheetBg
import com.betsudotai.shibari.presentation.ui.theme.TimelineSheetBtnBg

// ステータス表示用の情報
private data class StatusInfo(
    val label: String,
    val bg: Color,
    val fg: Color,
    val title: String,
    val desc: String,
)

private fun statusInfoOf(status: PostStatus): StatusInfo = when (status) {
    PostStatus.PENDING -> StatusInfo(
        label = "審査中",
        bg = StatusPendingBg,
        fg = StatusPendingFg,
        title = "🕐 審査中",
        desc = "友達がこの投稿をジャッジしています。縛りを守れているか、みんなが「✓ OK」か「✗ 違反」で投票中。" +
                "過半数の票が集まると結果が確定します。",
    )
    PostStatus.APPROVED -> StatusInfo(
        label = "承認済",
        bg = StatusApprovedBg,
        fg = StatusApprovedFg,
        title = "✅ 承認済み",
        desc = "友達の投票により、縛りを守れていると認められました！この調子で続けましょう。",
    )
    PostStatus.REJECTED -> StatusInfo(
        label = "否認",
        bg = StatusRejectedBg,
        fg = StatusRejectedFg,
        title = "❌ 否認",
        desc = "友達の投票により、縛りに違反していると判定されました。" +
                "ルールをもう一度確認して、次回はクリアしよう！",
    )
    PostStatus.DISPUTED -> StatusInfo(
        label = "異議あり",
        bg = StatusPendingBg,
        fg = StatusPendingFg,
        title = "⚠️ 異議あり",
        desc = "この投稿は現在異議申し立て中です。追加の投票が進行中です。",
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusBadge(
    status: PostStatus,
    modifier: Modifier = Modifier,
) {
    val info = statusInfoOf(status)
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = modifier
            .clickable { showSheet = true }
            .background(info.bg, RoundedCornerShape(20.dp))
            .border(1.dp, info.fg.copy(alpha = 0.27f), RoundedCornerShape(20.dp))
            .padding(start = 10.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = info.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = info.fg,
                letterSpacing = 0.2.sp,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "ステータス説明",
                modifier = Modifier.size(14.dp),
                tint = info.fg,
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = TimelineSheetBg,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = info.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = info.desc,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TimelineSheetBtnBg,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                ) {
                    Text(
                        text = "閉じる",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
