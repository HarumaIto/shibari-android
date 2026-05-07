package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betsudotai.shibari.domain.model.TimelinePost
import com.betsudotai.shibari.presentation.ui.components.TimelinePostItem
import com.betsudotai.shibari.presentation.viewmodel.timeline.TimelineUiState
import com.betsudotai.shibari.presentation.viewmodel.timeline.TimelineViewModel
import java.time.DayOfWeek
import java.time.LocalDate

// 投稿日時で絞り込むフィルター
enum class TimelineFilter(val label: String) {
    TODAY("今日"),
    WEEK("今週"),
    MONTH("今月"),
}

private fun TimelinePost.matchesFilter(filter: TimelineFilter, today: LocalDate): Boolean {
    val date = createdAt?.toLocalDate() ?: return false
    return when (filter) {
        TimelineFilter.TODAY -> date == today
        TimelineFilter.WEEK -> {
            val startOfWeek = today.with(DayOfWeek.MONDAY)
            !date.isBefore(startOfWeek) && !date.isAfter(today)
        }
        TimelineFilter.MONTH -> {
            val startOfMonth = today.withDayOfMonth(1)
            !date.isBefore(startOfMonth) && !date.isAfter(today)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onNavigateToComment: (String) -> Unit,
    onNavigateToGroup: () -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var reportingPostId by remember { mutableStateOf<String?>(null) }
    var reportingUserId by remember { mutableStateOf<String?>(null) }
    var reportReason by remember { mutableStateOf("") }

    var activeFilter by remember { mutableStateOf(TimelineFilter.TODAY) }
    val listState = rememberLazyListState()

    LaunchedEffect(activeFilter) {
        listState.scrollToItem(0)
    }

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
                        viewModel.reportPost(
                            reportingUserId!!,
                            reportingPostId!!,
                            reportReason
                        )
                        reportingPostId = null
                        reportingUserId = null
                        reportReason = ""
                    },
                    enabled = reportReason.isNotBlank()
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

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "タイムライン",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { onNavigateToGroup() }) {
                                Icon(Icons.Default.People, contentDescription = "グループ一覧")
                            }
                        },
                        actions = {
                            IconButton(onClick = { onNavigateToNotifications() }) {
                                Icon(Icons.Default.Notifications, contentDescription = "通知一覧")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )

                    // 投稿が読み込まれていればカウントを表示する
                    val allPosts = (uiState as? TimelineUiState.Success)?.posts.orEmpty()
                    val today = LocalDate.now()
                    FilterTabRow(
                        activeFilter = activeFilter,
                        onSelect = { activeFilter = it },
                        countOf = { filter ->
                            allPosts.count { it.matchesFilter(filter, today) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    val today = LocalDate.now()
                    val filteredPosts = remember(state.posts, activeFilter) {
                        state.posts.filter { it.matchesFilter(activeFilter, today) }
                    }

                    if (filteredPosts.isEmpty()) {
                        Text(
                            text = "${activeFilter.label}の投稿はまだありません",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                        ) {
                            items(filteredPosts.size) { index ->
                                val post = filteredPosts[index]
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
}

@Composable
private fun FilterTabRow(
    activeFilter: TimelineFilter,
    onSelect: (TimelineFilter) -> Unit,
    countOf: (TimelineFilter) -> Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimelineFilter.entries.forEach { filter ->
            val active = filter == activeFilter
            FilterPill(
                label = filter.label,
                count = countOf(filter),
                active = active,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    active: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
    val labelColor = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val countColor = if (active) MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                color = countColor,
            )
        }
    }
}
