package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SideOverlay(
    creatorName: String,
    avatarText: String,
    likesCount: Int,
    commentsCount: Int,
    isLiked: Boolean,
    isBookmarked: Boolean,
    isPlaying: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var followClicked by remember { mutableStateOf(false) }

    // Spring scaling when liking for a tactile physical bounce
    val likeScale = remember { Animatable(1f) }
    val bookmarkScale = remember { Animatable(1f) }

    // Rotating vinyl disk helper state
    val infiniteTransition = rememberInfiniteTransition(label = "music_rotation")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    Column(
        modifier = modifier
            .width(64.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Creator info / Follower Badge
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFEA4335), Color(0xFF4285F4))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarText.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (!followClicked) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFD0BCFF), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            followClicked = true
                        }
                        .testTag("follow_creator_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Follow Creator",
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // 2. Like button with physical pop & glass aesthetic
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ActionIconButton(
                testTag = "like_video_button",
                onClick = {
                    onLikeClick()
                    coroutineScope.launch {
                        likeScale.animateTo(1.4f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                        likeScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy))
                    }
                },
                scale = likeScale.value
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFFF3377) else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatCount(likesCount),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 3. Comments Drawer Trigger
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ActionIconButton(
                testTag = "comment_button",
                onClick = onCommentClick,
                scale = 1f
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatCount(commentsCount),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 4. Bookmark / Favorite
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ActionIconButton(
                testTag = "bookmark_button",
                onClick = {
                    onBookmarkClick()
                    coroutineScope.launch {
                        bookmarkScale.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                        bookmarkScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy))
                    }
                },
                scale = bookmarkScale.value
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save Bookmark",
                    tint = if (isBookmarked) Color(0xFFFFCC00) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Save",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 5. Classic vinyl Disk rotating decoration (spins only if playing)
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(42.dp)
                .rotate(if (isPlaying) vinylRotation else 0f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2E2E2E), Color(0xFF0F0F0F), Color(0xFF000000))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Stylized silver core of the LP vinyl record
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
fun ColumnScope.ActionIconButton(
    testTag: String,
    onClick: () -> Unit,
    scale: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .rotate(scale * 0.1f) // Subtle skew on click depth
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
        count >= 1_000 -> "%.1fk".format(count / 1_000f)
        else -> count.toString()
    }
}
