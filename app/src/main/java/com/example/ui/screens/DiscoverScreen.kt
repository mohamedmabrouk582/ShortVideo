package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ShortVideo
import com.example.ui.components.VideoPlayerComponent
import com.example.ui.viewmodel.ShortVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: ShortVideoViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videoFeed.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var activePreviewVideo by remember { mutableStateOf<ShortVideo?>(null) }

    val trendingHashtags = listOf(
        "#aesthetic", "#skateboarding", "#oceanlife", "#cosmos", "#naturelovers", "#lofichill"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // 1. Search TextField Panel
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search creators, topics, or hashtags", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Discover Trending hashtags lists
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.TrendingUp, contentDescription = "Trending", tint = Color(0xFFD0BCFF), modifier = Modifier.size(18.dp))
            Text("Trending Topics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(trendingHashtags) { hashtag ->
                SuggestionChip(
                    onClick = {
                        // If matching already, toggle search off, else pre-fill search
                        if (searchQuery == hashtag) {
                            viewModel.onSearchQueryChanged("")
                        } else {
                            viewModel.onSearchQueryChanged(hashtag)
                        }
                    },
                    label = { Text(hashtag, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (searchQuery == hashtag) Color(0xFFD0BCFF) else Color.White.copy(alpha = 0.08f),
                        labelColor = if (searchQuery == hashtag) Color(0xFF381E72) else Color.White
                    ),
                    border = if (searchQuery == hashtag) null else SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = Color.White.copy(alpha = 0.15f),
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Main Double Column Grid for items
        Text(
            text = if (searchQuery.isNotEmpty()) "Search Results" else "Explore",
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No videos match \"$searchQuery\" 🔍",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("discover_grid_list")
            ) {
                items(videos, key = { it.id }) { video ->
                    VideoPreviewCard(video = video, onClick = { activePreviewVideo = video })
                }
            }
        }
    }

    // Modal popup full player for premium discover experience
    activePreviewVideo?.let { video ->
        Dialog(
            onDismissRequest = { activePreviewVideo = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                VideoPlayerComponent(
                    videoUrl = video.videoUrl,
                    isCurrentlyActive = true,
                    filterName = video.filter,
                    bgMusicUrl = video.bgMusicUrl,
                    bgMusicVolume = video.bgMusicVolume,
                    originalVideoVolume = video.originalVideoVolume,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay details and close triggers
                Text(
                    text = video.creatorName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp)
                )

                IconButton(
                    onClick = { activePreviewVideo = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close player",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPreviewCard(video: ShortVideo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color(0xFF18181A))
                    )
                )
        ) {
            // Stylized central mini play button icon to simulate a snapshot video stream
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Tap to Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom metadata info card overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = video.creatorName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = video.description,
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Views count",
                        tint = Color.Gray,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${formatCount(video.viewsCount)} views",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
        count >= 1_000 -> "%.1fk".format(count / 1_000f)
        else -> count.toString()
    }
}
