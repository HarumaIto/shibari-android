package com.betsudotai.shibari.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betsudotai.shibari.domain.model.timeline.AiJudgment
import com.betsudotai.shibari.domain.value.JudgmentResult
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AiJudgmentDisplay(aiJudgment: AiJudgment) {
    val (backgroundColor, borderColor, contentColor) = when (aiJudgment.result) {
        JudgmentResult.APPROVE -> Triple(
            Color(0xFF00C853).copy(alpha = 0.1f), // Green background
            Color(0xFF00C853), // Green border
            Color(0xFF00C853) // Green text
        )
        JudgmentResult.REJECT -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f), // Red background
            MaterialTheme.colorScheme.error, // Red border
            MaterialTheme.colorScheme.error // Red text
        )
        JudgmentResult.UNKNOWN -> Triple(
            Color.Gray.copy(alpha = 0.1f), // Gray background
            Color.Gray, // Gray border
            Color.Gray // Gray text
        )
    }

    val judgmentText = when (aiJudgment.result) {
        JudgmentResult.APPROVE -> "AI判定: 承認"
        JudgmentResult.REJECT -> "AI判定: 否認"
        JudgmentResult.UNKNOWN -> "AI判定: 判定不能"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Judgment",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = judgmentText,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                val dateFormatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                Text(
                    text = dateFormatter.format(aiJudgment.judgedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = aiJudgment.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}