package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.SideOverlay
import com.example.ui.components.VideoDescriptionOverlay
import com.example.ui.components.VideoPlayerComponent
import com.example.ui.viewmodel.ShortVideoViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFeedScreen(
    viewModel: ShortVideoViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videoFeed.collectAsStateWithLifecycle()
    val comments by viewModel.activeComments.collectAsStateWithLifecycle()

    var isCommentsOpen by remember { mutableStateOf(false) }
    var selectedVideoIdForComments by remember { mutableStateOf<Long?>(null) }

    // Check if feed is empty
    if (videos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFFD0BCFF))
                Text(
                    text = "Loading amazing shorts... 🎬",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    // Configure vertical paging state
    val pagerState = rememberPagerState(pageCount = { videos.size })

    // Track views dynamically when a pager selection aligns to active item
    LaunchedEffect(pagerState.currentPage, videos) {
        if (pagerState.currentPage in videos.indices) {
            val currentVideo = videos[pagerState.currentPage]
            viewModel.trackView(currentVideo.id)
            
            // Sync current active comments video identifier
            if (isCommentsOpen) {
                viewModel.setActiveVideoForComments(currentVideo.id)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Full Page Vertical View
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page in videos.indices) {
                val video = videos[page]
                val isCurrentlyActive = (page == pagerState.currentPage) && !isCommentsOpen

                Box(modifier = Modifier.fillMaxSize()) {
                    // Underlay MP4 Loop Feed component
                    VideoPlayerComponent(
                        videoUrl = video.videoUrl,
                        isCurrentlyActive = isCurrentlyActive,
                        filterName = video.filter,
                        bgMusicUrl = video.bgMusicUrl,
                        bgMusicVolume = video.bgMusicVolume,
                        originalVideoVolume = video.originalVideoVolume,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay bottom details scrim
                    VideoDescriptionOverlay(
                        creatorName = video.creatorName,
                        description = video.description,
                        musicTrack = video.musicTrack,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 56.dp) // Leave spacing for Bottom Navigation padding
                    )

                    // Overlay side vertical action dock
                    SideOverlay(
                        creatorName = video.creatorName,
                        avatarText = video.avatarUrl,
                        likesCount = video.likesCount,
                        commentsCount = video.viewsCount / 12 + video.isLiked.let { if (it) 1 else 0 }, // synthetic comment count base
                        isLiked = video.isLiked,
                        isBookmarked = video.isBookmarked,
                        isPlaying = isCurrentlyActive,
                        onLikeClick = { viewModel.toggleLike(video.id) },
                        onBookmarkClick = { viewModel.toggleBookmark(video.id) },
                        onCommentClick = {
                            selectedVideoIdForComments = video.id
                            viewModel.setActiveVideoForComments(video.id)
                            isCommentsOpen = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 80.dp, end = 8.dp)
                    )
                }
            }
        }

        // Live interactive bottom comments sliding overlay
        CommentBottomSheet(
            isOpen = isCommentsOpen,
            comments = comments,
            onClose = {
                isCommentsOpen = false
                viewModel.setActiveVideoForComments(null)
            },
            onSendComment = { newComment ->
                selectedVideoIdForComments?.let { id ->
                    viewModel.addComment(id, newComment, "@visitor_prime")
                }
            }
        )
    }
}
