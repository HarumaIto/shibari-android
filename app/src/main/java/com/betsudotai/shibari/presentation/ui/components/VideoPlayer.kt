package com.betsudotai.shibari.presentation.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }

    // ExoPlayerのインスタンスを作成・記憶
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            seekTo(0L)

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isLoading = playbackState == Player.STATE_BUFFERING
                }
            })
        }
    }

    // Composableが破棄される時にプレイヤーも解放する（メモリリーク防止）
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }


    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    player!!.playWhenReady = false // 複数同時に再生されないように最初は自動再生OFF
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                }
            },
            modifier = modifier
        )
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary // 必要に応じて色を変更できます（例: Color.Red）
            )
        }
    }
}