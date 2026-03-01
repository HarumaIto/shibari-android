package com.betsudotai.shibari.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.betsudotai.shibari.presentation.ui.theme.ShibariTheme
import com.betsudotai.shibari.presentation.viewmodel.profileSetup.ProfileSetupEvent
import com.betsudotai.shibari.presentation.viewmodel.profileSetup.ProfileSetupViewModel

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onNavigateToGroupSelection: () -> Unit
) {
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

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
        onSaveClick = { viewModel.saveProfile(context) },
        imageToShow = selectedImageUri,
        onImageChange = {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    )
}

@Composable
fun ProfileSetupScreenContent(
    snackbarHostState: SnackbarHostState,
    displayName: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    imageToShow: Uri?,
    onImageChange: () -> Unit
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
            Text("アプリ内で表示される名前と画像を入力してください", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        onImageChange()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageToShow != null) {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = "Profile Icon",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "プロフィール画像を選択",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onImageChange) {
                Text("画像を変更")
            }

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
            onSaveClick = {},
            imageToShow = null,
            onImageChange = {}
        )
    }
}