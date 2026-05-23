package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerComponent(
    videoUrl: String,
    isCurrentlyActive: Boolean,
    modifier: Modifier = Modifier,
    filterName: String = "None",
    bgMusicUrl: String = "None",
    bgMusicVolume: Float = 0.5f,
    originalVideoVolume: Float = 1.0f
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(isCurrentlyActive) }
    var showPauseIndicator by remember { mutableStateOf(false) }

    // Initialize ExoPlayer and configure its volume based on originalVideoVolume setting
    val exoPlayer = remember(videoUrl) {
        val cacheDataSourceFactory = com.example.data.VideoCacheManager.getCacheDataSourceFactory(context)
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = isCurrentlyActive
                volume = originalVideoVolume
                
                // Setup Media Item
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                prepare()
            }
    }

    // Dynamic Volume Updates for original audio
    LaunchedEffect(originalVideoVolume) {
        exoPlayer.volume = originalVideoVolume
    }

    // Initialize secondary audio track player for loop music
    val bgPlayer = remember(bgMusicUrl) {
        if (bgMusicUrl != "None" && bgMusicUrl.isNotBlank()) {
            android.media.MediaPlayer().apply {
                try {
                    setDataSource(bgMusicUrl)
                    isLooping = true
                    setVolume(bgMusicVolume, bgMusicVolume)
                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            null
        }
    }

    // Sync volume ratio dynamically
    LaunchedEffect(bgMusicVolume) {
        try {
            bgPlayer?.setVolume(bgMusicVolume, bgMusicVolume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Sync active and play state from vertical pager swiping and screen interaction
    LaunchedEffect(isCurrentlyActive, isPlaying) {
        val playState = isCurrentlyActive && isPlaying
        exoPlayer.playWhenReady = playState
        try {
            if (bgPlayer != null) {
                if (playState) {
                    bgPlayer.start()
                } else {
                    bgPlayer.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Free resources safely when page is destroyed/scrolled out
    DisposableEffect(videoUrl, bgMusicUrl) {
        onDispose {
            exoPlayer.release()
            try {
                bgPlayer?.stop()
                bgPlayer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Custom touch overlay to provide a clean play-pause user controller
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(videoUrl, bgMusicUrl) {
                detectTapGestures {
                    isPlaying = !isPlaying
                    showPauseIndicator = true
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom swipe overlays instead
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Fill the crop
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Real-time custom Visual Filters & Effects Renderer
        FilterOverlay(
            filterName = filterName,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay flashing indicator for pause status
        AnimatedVisibility(
            visible = !isPlaying,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.5f),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.5f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Unmute or Play",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun FilterOverlay(filterName: String, modifier: Modifier = Modifier) {
    when (filterName) {
        "Sepia" -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFF704213).copy(alpha = 0.22f),
                    blendMode = BlendMode.Color
                )
                drawRect(
                    color = Color(0xFFE4A951).copy(alpha = 0.08f),
                    blendMode = BlendMode.Overlay
                )
            }
        }
        "Black and White" -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Black,
                    blendMode = BlendMode.Saturation
                )
            }
        }
        "Vintage" -> {
            Box(modifier = modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color(0xFFFF8F00).copy(alpha = 0.14f),
                        blendMode = BlendMode.Color
                    )
                    drawRect(
                        color = Color(0xFF5D4037).copy(alpha = 0.08f),
                        blendMode = BlendMode.Overlay
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color(0xFF241508).copy(alpha = 0.5f)),
                                radius = 1100f
                            )
                        )
                )
            }
        }
        "Glitch" -> {
            val infiniteTransition = rememberInfiniteTransition(label = "GlitchEffect")
            
            val scanY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scanlineY"
            )
            
            val triggerOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset"
            )

            val glitchAlpha by infiniteTransition.animateFloat(
                initialValue = 0.03f,
                targetValue = 0.16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glitchAlpha"
            )

            Box(modifier = modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color.Cyan.copy(alpha = glitchAlpha),
                        blendMode = BlendMode.Screen
                    )
                    drawRect(
                        color = Color.Magenta.copy(alpha = glitchAlpha * 0.75f),
                        blendMode = BlendMode.Overlay
                    )
                }
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val height = size.height
                    val width = size.width
                    
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = androidx.compose.ui.geometry.Offset(0f, scanY * height),
                        end = androidx.compose.ui.geometry.Offset(width, scanY * height),
                        strokeWidth = 3.dp.toPx()
                    )

                    if (triggerOffset > 6f) {
                        drawRect(
                            color = Color(0xFFEADDFF).copy(alpha = 0.18f),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, height * 0.35f),
                            size = androidx.compose.ui.geometry.Size(width, 14.dp.toPx())
                        )
                        drawRect(
                            color = Color.White.copy(alpha = 0.1f),
                            topLeft = androidx.compose.ui.geometry.Offset(10.dp.toPx(), height * 0.37f),
                            size = androidx.compose.ui.geometry.Size(width - 20.dp.toPx(), 4.dp.toPx())
                        )
                        drawRect(
                            color = Color(0xFFD0BCFF).copy(alpha = 0.18f),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, height * 0.68f),
                            size = androidx.compose.ui.geometry.Size(width, 10.dp.toPx())
                        )
                    }
                }
            }
        }
        "Warm Tone" -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFFFFB300).copy(alpha = 0.12f),
                    blendMode = BlendMode.Color
                )
            }
        }
        "Cool Breeze" -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFF00B0FF).copy(alpha = 0.12f),
                    blendMode = BlendMode.Color
                )
            }
        }
        "Sunset Glow" -> {
            Box(modifier = modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color(0xFFE91E63).copy(alpha = 0.08f),
                        blendMode = BlendMode.Color
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF9100).copy(alpha = 0.18f),
                                    Color(0xFF6200EA).copy(alpha = 0.18f)
                                )
                            )
                        )
                )
            }
        }
        "Cine Gray" -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Black,
                    blendMode = BlendMode.Saturation
                )
                drawRect(
                    color = Color.White.copy(alpha = 0.04f),
                    blendMode = BlendMode.Overlay
                )
            }
        }
        "Neon Cyan" -> {
            Canvas(modifier = modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                    blendMode = BlendMode.Color
                )
            }
        }
        else -> {
            // None or normal playback
        }
    }
}

