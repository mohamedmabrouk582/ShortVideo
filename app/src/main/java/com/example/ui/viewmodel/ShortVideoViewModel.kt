package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ShortVideo
import com.example.data.model.VideoComment
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShortVideoViewModel(private val repository: VideoRepository) : ViewModel() {

    init {
        // Pre-populate with our awesome curated short videos on startup
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Main responsive video list, filtered by search query
    val videoFeed: StateFlow<List<ShortVideo>> = combine(
        repository.allVideos,
        _searchQuery
    ) { videos, query ->
        if (query.isBlank()) {
            videos
        } else {
            videos.filter { video ->
                video.description.contains(query, ignoreCase = true) ||
                video.creatorName.contains(query, ignoreCase = true) ||
                video.hashtags.contains(query, ignoreCase = true)
            }
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered list of videos bookmarked by user
    val bookmarkedVideos: StateFlow<List<ShortVideo>> = repository.allVideos
        .map { list -> list.filter { it.isBookmarked } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered list of videos liked by user
    val likedVideos: StateFlow<List<ShortVideo>> = repository.allVideos
        .map { list -> list.filter { it.isLiked } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State of the comments section for currently viewed videoId
    private val _activeVideoIdForComments = MutableStateFlow<Long?>(null)
    val activeComments: StateFlow<List<VideoComment>> = _activeVideoIdForComments
        .flatMapLatest { videoId ->
            if (videoId == null) flowOf(emptyList())
            else repository.getCommentsForVideo(videoId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setActiveVideoForComments(videoId: Long?) {
        _activeVideoIdForComments.value = videoId
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleLike(videoId: Long) {
        viewModelScope.launch {
            repository.toggleLike(videoId)
        }
    }

    fun toggleBookmark(videoId: Long) {
        viewModelScope.launch {
            repository.toggleBookmark(videoId)
        }
    }

    fun addComment(videoId: Long, commentText: String, username: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            repository.addComment(videoId, username, commentText)
        }
    }

    fun trackView(videoId: Long) {
        viewModelScope.launch {
            repository.incrementViewCount(videoId)
        }
    }

    fun uploadVideo(
        creator: String,
        description: String,
        musicTrack: String,
        hashtags: String,
        templateType: String,
        filter: String = "None",
        bgMusicUrl: String = "None",
        bgMusicVolume: Float = 0.5f,
        originalVideoVolume: Float = 1.0f
    ) {
        viewModelScope.launch {
            val videoUrl = when (templateType) {
                "Skaters" -> "https://assets.mixkit.co/videos/preview/mixkit-skater-doing-a-trick-in-a-skatepark-42045-large.mp4"
                "Gaming" -> "https://assets.mixkit.co/videos/preview/mixkit-hands-of-a-gamer-playing-with-a-controller-41618-large.mp4"
                "Cooking" -> "https://assets.mixkit.co/videos/preview/mixkit-chef-plating-a-gourmet-dish-42510-large.mp4"
                else -> "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4"
            }

            val formattedHashtags = hashtags.trim().split(" ")
                .joinToString(" ") { if (it.startsWith("#") || it.isEmpty()) it else "#$it" }

            val initialShortVideo = ShortVideo(
                videoUrl = videoUrl,
                creatorName = if (creator.startsWith("@")) creator else "@$creator",
                avatarUrl = creator.take(2).uppercase().ifEmpty { "ME" },
                description = "$description $formattedHashtags",
                musicTrack = musicTrack.ifBlank { "Original Audio" },
                likesCount = 0,
                viewsCount = 1,
                hashtags = formattedHashtags,
                filter = filter,
                bgMusicUrl = bgMusicUrl,
                bgMusicVolume = bgMusicVolume,
                originalVideoVolume = originalVideoVolume
            )
            repository.insertVideo(initialShortVideo)
        }
    }

    // Factory to instantiate the ViewModel cleanly with constructor parameters
    class Factory(private val repository: VideoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShortVideoViewModel::class.java)) {
                return ShortVideoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
