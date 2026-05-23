package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoComment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    isOpen: Boolean,
    comments: List<VideoComment>,
    onClose: () -> Unit,
    onSendComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newCommentText by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 250)
        ) + fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim background tapping closes the comment sheet
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Actual comment contents block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f) // Take 65% height
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .clickable(enabled = false) {}, // Intercept clicks inside card
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1C24).copy(alpha = 0.94f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())
                ) {
                    // Header Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${comments.size} Comments",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.testTag("comments_header_title")
                        )

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close comments",
                                tint = Color.LightGray
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Comments Lazy List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No comments yet. Start the conversation! 💬",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            items(comments, key = { it.commentId }) { comment ->
                                CommentItemRow(comment = comment)
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // bottom typing bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f))
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // User Avatar Simulation
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Input field
                        TextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = {
                                Text(
                                    text = "Add comment...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                .testTag("comment_input_field"),
                            maxLines = 3
                        )

                        // Send button
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onSendComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            enabled = newCommentText.isNotBlank(),
                            modifier = Modifier
                                .testTag("comment_send_button")
                                .background(
                                    color = if (newCommentText.isNotBlank()) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.08f),
                                    shape = CircleShape
                                )
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Post Comment",
                                tint = if (newCommentText.isNotBlank()) Color(0xFF381E72) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItemRow(comment: VideoComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (comment.username.hashCode() % 2 == 0) {
                            listOf(Color(0xFFFF5E62), Color(0xFFFF9966))
                        } else {
                            listOf(Color(0xFF11998E), Color(0xFF38EF7D))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val initial = comment.username.replace("@", "").take(2).uppercase()
            Text(
                text = initial.ifEmpty { "U" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = comment.username,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = formatCommentTime(comment.timestamp),
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Text(
                text = comment.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

private fun formatCommentTime(timeMs: Long): String {
    val diff = System.currentTimeMillis() - timeMs
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val date = Date(timeMs)
            val format = SimpleDateFormat("MMM dd", Locale.getDefault())
            format.format(date)
        }
    }
}
