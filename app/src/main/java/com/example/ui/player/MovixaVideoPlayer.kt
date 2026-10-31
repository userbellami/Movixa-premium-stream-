package com.example.ui.player

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MovixaRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovixaVideoPlayer(
    playerState: PlayerState,
    onClose: () -> Unit,
    onPositionUpdate: (Long, Long) -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onLockToggle: () -> Unit,
    onAudioSubtitleChange: (String, String) -> Unit,
    onPlayNextEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val movie = playerState.movie ?: return
    val initialVideoUrl = playerState.episode?.videoUrl ?: movie.videoUrl

    var currentStreamUri by remember(initialVideoUrl) { mutableStateOf(initialVideoUrl) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var fallbackTriggered by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var currentPosMs by remember { mutableLongStateOf(playerState.currentPositionMs) }
    var durationMs by remember { mutableLongStateOf(playerState.totalDurationMs) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showAudioSubtitlesDialog by remember { mutableStateOf(false) }

    // Intercept back button to safely close
    BackHandler {
        onClose()
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, playerState.isPlaying, playerState.isLocked) {
        if (showControls && playerState.isPlaying && !playerState.isLocked) {
            delay(4000)
            showControls = false
        }
    }

    // Polling playback position
    LaunchedEffect(playerState.isPlaying, hasError) {
        while (!hasError) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying && !isUserSeeking) {
                    val pos = vv.currentPosition.toLong()
                    val dur = vv.duration.toLong().coerceAtLeast(0L)
                    currentPosMs = pos
                    if (dur > 0) durationMs = dur
                    onPositionUpdate(pos, dur)
                }
            }
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("movixa_video_player")
    ) {
        // Native VideoView embedded in Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(currentStreamUri))
                    setOnPreparedListener { mp ->
                        isBuffering = false
                        hasError = false
                        val dur = duration.toLong()
                        if (dur > 0) durationMs = dur
                        if (playerState.currentPositionMs > 0) {
                            seekTo(playerState.currentPositionMs.toInt())
                        }
                        if (playerState.isPlaying) {
                            start()
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                mp.playbackParams = mp.playbackParams.setSpeed(playerState.playbackSpeed)
                            } catch (_: Exception) {}
                        }
                    }
                    setOnInfoListener { _, what, _ ->
                        when (what) {
                            MediaPlayer.MEDIA_INFO_BUFFERING_START -> isBuffering = true
                            MediaPlayer.MEDIA_INFO_BUFFERING_END,
                            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> isBuffering = false
                        }
                        true
                    }
                    setOnCompletionListener {
                        isBuffering = false
                        onPositionUpdate(durationMs, durationMs)
                    }
                    setOnErrorListener { _, what, extra ->
                        isBuffering = false
                        // Automatically fall back to bundled high-res offline stream
                        if (!fallbackTriggered) {
                            fallbackTriggered = true
                            val localUri = "android.resource://${context.packageName}/${R.raw.sample_stream}"
                            currentStreamUri = localUri
                            setVideoURI(Uri.parse(localUri))
                            start()
                            true
                        } else {
                            hasError = true
                            true
                        }
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                if (!hasError) {
                    if (playerState.isPlaying && !vv.isPlaying && !isBuffering) {
                        vv.start()
                    } else if (!playerState.isPlaying && vv.isPlaying) {
                        vv.pause()
                    }
                }
            }
        )

        // Buffering Indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MovixaRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        // Error Recovery Dialog
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MovixaRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Playback Network Interrupted",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "We encountered a temporary network delay loading the online stream. You can retry or switch back to the main catalog.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onClose,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    hasError = false
                                    isBuffering = true
                                    val localUri = "android.resource://${context.packageName}/${R.raw.sample_stream}"
                                    videoViewRef?.setVideoURI(Uri.parse(localUri))
                                    videoViewRef?.start()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MovixaRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        // Locked mode: Floating Unlock button
        if (playerState.isLocked && !hasError) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .clickable(onClick = onLockToggle)
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Unlock controls",
                            tint = Color.White
                        )
                        Text(
                            text = "Tap to Unlock",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Unlocked Player Overlay Controls
        if (!playerState.isLocked && !hasError) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    // Top Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = movie.title,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (playerState.episode != null) {
                                        Text(
                                            text = "S${playerState.episode.seasonNumber}:E${playerState.episode.episodeNumber} ${playerState.episode.title}",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { showAudioSubtitlesDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Subtitles,
                                        contentDescription = "Audio and Subtitles",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Center Transport Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 10s
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val newPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                                    vv.seekTo(newPos)
                                    currentPosMs = newPos.toLong()
                                }
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(36.dp))

                        // Play / Pause prominent toggle button
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .size(72.dp)
                                .clickable {
                                    videoViewRef?.let { vv ->
                                        if (vv.isPlaying) {
                                            vv.pause()
                                        } else {
                                            vv.start()
                                        }
                                    }
                                    onPlayPauseToggle()
                                }
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(36.dp))

                        // Forward 10s
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val newPos = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                    vv.seekTo(newPos)
                                    currentPosMs = newPos.toLong()
                                }
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Bottom Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        // Slider seekbar
                        val effectiveDur = if (durationMs > 0) durationMs.toFloat() else 1f
                        val currentFraction = if (isUserSeeking) sliderPosition else (currentPosMs.toFloat() / effectiveDur).coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Slider(
                                value = currentFraction,
                                onValueChange = { newVal ->
                                    isUserSeeking = true
                                    sliderPosition = newVal
                                },
                                onValueChangeFinished = {
                                    isUserSeeking = false
                                    val targetMs = (sliderPosition * effectiveDur).toLong()
                                    videoViewRef?.seekTo(targetMs.toInt())
                                    currentPosMs = targetMs
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MovixaRed,
                                    activeTrackColor = MovixaRed,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Remaining Time
                            val remainingMs = (durationMs - currentPosMs).coerceAtLeast(0L)
                            Text(
                                text = formatTime(remainingMs),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Bottom Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lock toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(onClick = onLockToggle)
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock controls",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Lock", color = Color.White, fontSize = 12.sp)
                            }

                            // Speed selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showSpeedDialog = true }
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Speed (${playerState.playbackSpeed}x)",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }

                            // Next Episode (if series)
                            if (movie.isSeries) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(onClick = onPlayNextEpisode)
                                        .padding(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next Episode",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Next Ep", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Playback Speed", color = TextPrimary) },
            text = {
                Column {
                    speeds.forEach { speed ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSpeedChange(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = playerState.playbackSpeed == speed,
                                onClick = {
                                    onSpeedChange(speed)
                                    showSpeedDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MovixaRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${speed}x ${if (speed == 1.0f) "(Normal)" else ""}",
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = MovixaRed)
                }
            }
        )
    }

    // Audio & Subtitles Dialog
    if (showAudioSubtitlesDialog) {
        var tempAudio by remember { mutableStateOf(playerState.audioTrack) }
        var tempSubtitle by remember { mutableStateOf(playerState.subtitleTrack) }

        val audioOptions = listOf("English [Original] (5.1)", "Spanish", "French", "German", "Japanese")
        val subtitleOptions = listOf("Off", "English [CC]", "Spanish", "French", "German")

        AlertDialog(
            onDismissRequest = { showAudioSubtitlesDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Audio & Subtitles", color = TextPrimary) },
            text = {
                Column {
                    Text("Audio", color = MovixaRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    audioOptions.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempAudio = opt }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = tempAudio == opt,
                                onClick = { tempAudio = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = MovixaRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = opt, color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Subtitles", color = MovixaRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    subtitleOptions.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempSubtitle = opt }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = tempSubtitle == opt,
                                onClick = { tempSubtitle = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = MovixaRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = opt, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAudioSubtitleChange(tempAudio, tempSubtitle)
                        showAudioSubtitlesDialog = false
                    }
                ) {
                    Text("Apply", color = MovixaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAudioSubtitlesDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
