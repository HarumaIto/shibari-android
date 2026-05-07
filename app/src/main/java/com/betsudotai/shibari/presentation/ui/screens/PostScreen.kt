package com.betsudotai.shibari.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.betsudotai.shibari.core.util.FileUtil
import com.betsudotai.shibari.presentation.ui.components.VideoPlayer
import com.betsudotai.shibari.presentation.viewmodel.post.PostEvent
import com.betsudotai.shibari.presentation.viewmodel.post.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: PostViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val comment by viewModel.comment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // PhotoPickerのランチャー
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.onImageSelected(pendingCaptureUri)
            }
            pendingCaptureUri = null
        }
    )
    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success) {
                viewModel.onImageSelected(pendingCaptureUri)
            }
            pendingCaptureUri = null
        }
    )
    var pendingCameraAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                pendingCameraAction?.invoke()
            }
            pendingCameraAction = null
        }
    )
    var showSourceSheet by remember { mutableStateOf(false) }

    fun launchTakePicture() {
        val uri = FileUtil.createImageCaptureUri(context)
        pendingCaptureUri = uri
        takePictureLauncher.launch(uri)
    }

    fun launchCaptureVideo() {
        val uri = FileUtil.createVideoCaptureUri(context)
        pendingCaptureUri = uri
        captureVideoLauncher.launch(uri)
    }

    fun ensureCameraPermissionAndRun(action: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            action()
        } else {
            pendingCameraAction = action
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val mimeType = selectedImageUri?.let { context.contentResolver.getType(it) }
    val isVideo = mimeType?.startsWith("video/") == true

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is PostEvent.NavigateBack -> onNavigateBack()
                is PostEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("証拠を提出") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 画像プレビューエリア
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    if (isVideo) {
                        // ★動画プレビュー
                        VideoPlayer(
                            videoUri = selectedImageUri.toString(),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // ★画像プレビュー
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onImageSelected(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "メディアを解除",
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showSourceSheet = true },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Photo", modifier = Modifier.size(48.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // コメント入力
            OutlinedTextField(
                value = comment,
                onValueChange = viewModel::onCommentChange,
                label = { Text("一言コメント (任意)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            // 送信ボタン
            Button(
                onClick = { viewModel.submitPost(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && selectedImageUri != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("タイムラインに投稿する")
                }
            }
        }
    }

    if (showSourceSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                ListItem(
                    headlineContent = { Text("写真を撮影") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                    modifier = Modifier.clickable {
                        showSourceSheet = false
                        ensureCameraPermissionAndRun { launchTakePicture() }
                    }
                )
                ListItem(
                    headlineContent = { Text("動画を撮影") },
                    leadingContent = { Icon(Icons.Default.Videocam, null) },
                    modifier = Modifier.clickable {
                        showSourceSheet = false
                        ensureCameraPermissionAndRun { launchCaptureVideo() }
                    }
                )
                ListItem(
                    headlineContent = { Text("ギャラリーから選択") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable {
                        showSourceSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }
                )
            }
        }
    }
}
