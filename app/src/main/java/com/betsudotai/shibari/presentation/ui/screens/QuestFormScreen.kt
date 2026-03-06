package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betsudotai.shibari.domain.value.QuestFrequency
import com.betsudotai.shibari.domain.value.QuestType
import com.betsudotai.shibari.presentation.viewmodel.questForm.QuestFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestFormScreen(
    viewModel: QuestFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isLoading) "" else if (uiState.isEditMode) "縛りの編集" else "縛りの作成") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.isValid && !uiState.isSaving
                    ) {
                        Text("保存")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // タイトル
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("タイトル *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 詳細・条件
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("詳細・条件 *") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                // クエストの種類
                Text(
                    text = "クエストの種類",
                    style = MaterialTheme.typography.labelLarge
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    QuestType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = uiState.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = QuestType.entries.size
                            ),
                            label = { Text(type.displayName()) }
                        )
                    }
                }

                // 頻度
                Text(
                    text = "頻度",
                    style = MaterialTheme.typography.labelLarge
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    QuestFrequency.entries.forEachIndexed { index, frequency ->
                        SegmentedButton(
                            selected = uiState.frequency == frequency,
                            onClick = { viewModel.onFrequencyChange(frequency) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = QuestFrequency.entries.size
                            ),
                            label = { Text(frequency.displayName()) }
                        )
                    }
                }

                // 目標回数（任意）
                OutlinedTextField(
                    value = uiState.thresholdText,
                    onValueChange = { viewModel.onThresholdChange(it) },
                    label = { Text("目標回数（任意）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
