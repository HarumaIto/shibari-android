package com.betsudotai.shibari.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betsudotai.shibari.presentation.ui.theme.ShibariTheme
import com.betsudotai.shibari.presentation.viewmodel.profileSetup.ProfileSetupEvent
import com.betsudotai.shibari.presentation.viewmodel.profileSetup.ProfileSetupViewModel

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onNavigateToGroupSelection: () -> Unit
) {
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ProfileSetupEvent.NavigateToGroupSelection -> onNavigateToGroupSelection()
                is ProfileSetupEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ProfileSetupScreenContent(
        snackbarHostState = snackbarHostState,
        displayName = displayName,
        isLoading = isLoading,
        onNameChange = viewModel::onNameChange,
        onSaveClick = { viewModel.saveProfile() }
    )
}

@Composable
fun ProfileSetupScreenContent(
    snackbarHostState: SnackbarHostState,
    displayName: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("プロフィール設定", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("アプリ内で表示される名前を入力してください", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = onNameChange,
                label = { Text("表示名 (ニックネーム)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存して始める")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSetupScreenContentPreview() {
    ShibariTheme (
        darkTheme = true,
    ) {
        ProfileSetupScreenContent(
            displayName = "テストユーザー",
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onSaveClick = {}
        )
    }
}