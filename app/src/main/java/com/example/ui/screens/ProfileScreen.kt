package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun ProfileScreen(
    viewModel: ShortVideoViewModel,
    modifier: Modifier = Modifier
) {
    val bookmarkedVideos by viewModel.bookmarkedVideos.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()

    var isEditingHandle by remember { mutableStateOf(false) }
    var userHandle by remember { mutableStateOf("visitor_prime") }
    var activeGridTab by remember { mutableStateOf("Bookmarks") } // "Bookmarks" or "Liked"

    var activePlayVideo by remember { mutableStateOf<ShortVideo?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .statusBarsPadding()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Sleek top avatar panel
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userHandle.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        // Editable handle card
        if (isEditingHandle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = userHandle,
                    onValueChange = { userHandle = it.trim() },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_editable_textfield")
                )
                Button(
                    onClick = { isEditingHandle = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { isEditingHandle = true }
            ) {
                Text(
                    text = "@$userHandle",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.testTag("profile_display_handle")
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Name",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats grid indicators styled as a Frosted Glass container
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStatColumn(count = "84", label = "Following")
            VerticalDividerHelper()
            ProfileStatColumn(count = "1.2k", label = "Followers")
            VerticalDividerHelper()
            // Dynamic total likes from videos liked by this user
            ProfileStatColumn(count = "${8500 + likedVideos.size * 125}", label = "Likes")
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // 2. Twin Tab Selectors for video grids with modern styling
        TabRow(
            selectedTabIndex = if (activeGridTab == "Bookmarks") 0 else 1,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFD0BCFF),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeGridTab == "Bookmarks") 0 else 1]),
                    color = Color(0xFFD0BCFF)
                )
            }
        ) {
            Tab(
                selected = activeGridTab == "Bookmarks",
                onClick = { activeGridTab = "Bookmarks" },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Bookmarks (${bookmarkedVideos.size})", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                modifier = Modifier.testTag("profile_tab_bookmarks")
            )
            Tab(
                selected = activeGridTab == "Liked",
                onClick = { activeGridTab = "Liked" },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Liked (${likedVideos.size})", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                modifier = Modifier.testTag("profile_tab_liked")
            )
        }

        // Tab grids
        val currentTabVideos = if (activeGridTab == "Bookmarks") bookmarkedVideos else likedVideos

        if (currentTabVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (activeGridTab == "Bookmarks") "No bookmarks saved yet. 📁" else "No liked videos yet. ❤️",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentTabVideos, key = { it.id }) { video ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.05f), Color(0xFF18181A))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { activePlayVideo = video },
                        contentAlignment = Alignment.BottomStart
                    ) {
                        // Tiny views count overlay
                        Text(
                            text = "${video.creatorName}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal popup full player for profile items
    activePlayVideo?.let { video ->
        Dialog(
            onDismissRequest = { activePlayVideo = null },
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

                Text(
                    text = "Viewing: ${video.creatorName}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp)
                )

                IconButton(
                    onClick = { activePlayVideo = null },
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
fun ProfileStatColumn(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun VerticalDividerHelper() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color.White.copy(alpha = 0.15f))
    )
}
