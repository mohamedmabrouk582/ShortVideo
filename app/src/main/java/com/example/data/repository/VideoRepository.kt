package com.example.data.repository

import com.example.data.database.VideoDao
import com.example.data.model.ShortVideo
import com.example.data.model.VideoComment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class VideoRepository(private val videoDao: VideoDao) {

    val allVideos: Flow<List<ShortVideo>> = videoDao.getAllVideos()

    fun getCommentsForVideo(videoId: Long): Flow<List<VideoComment>> {
        return videoDao.getCommentsForVideo(videoId)
    }

    suspend fun insertVideo(video: ShortVideo): Long {
        return videoDao.insertVideo(video)
    }

    suspend fun toggleLike(videoId: Long) {
        val video = videoDao.getVideoById(videoId) ?: return
        val updatedLikeState = !video.isLiked
        val updatedLikesCount = if (updatedLikeState) video.likesCount + 1 else video.likesCount - 1
        videoDao.updateVideo(video.copy(
            isLiked = updatedLikeState,
            likesCount = updatedLikesCount.coerceAtLeast(0)
        ))
    }

    suspend fun toggleBookmark(videoId: Long) {
        val video = videoDao.getVideoById(videoId) ?: return
        val updatedBookmarkState = !video.isBookmarked
        videoDao.updateVideo(video.copy(
            isBookmarked = updatedBookmarkState
        ))
    }

    suspend fun addComment(videoId: Long, username: String, text: String) {
        val comment = VideoComment(
            videoId = videoId,
            username = username,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        videoDao.insertComment(comment)
    }

    suspend fun incrementViewCount(videoId: Long) {
        val video = videoDao.getVideoById(videoId) ?: return
        videoDao.updateVideo(video.copy(
            viewsCount = video.viewsCount + 1
        ))
    }

    suspend fun seedDatabaseIfEmpty() {
        val currentVideos = videoDao.getAllVideos().first()
        if (currentVideos.isEmpty()) {
            val initialVideos = listOf(
                ShortVideo(
                    id = 1,
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-girl-in-neon-sign-sitting-on-ground-reading-40345-large.mp4",
                    creatorName = "@neon_reader",
                    avatarUrl = "NR",
                    description = "Midnight reading sessions under neon lights are magical... 📖✨ Best place to get lost in a story. #aesthetic #neonvibes #reading #lofichill",
                    musicTrack = "Lofi Chills - Sunset Vibes",
                    likesCount = 1420,
                    viewsCount = 8900,
                    hashtags = "#aesthetic #neonvibes #reading #lofichill"
                ),
                ShortVideo(
                    id = 2,
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-skater-doing-a-trick-in-a-skatepark-42232-large.mp4",
                    creatorName = "@skate_pro",
                    avatarUrl = "SP",
                    description = "Nailing this new flip trick on a sunny afternoon! 🛹🔥 Persistence pays off. #skateboarding #skatelife #extremesports #training",
                    musicTrack = "Hyperactive Punk Rock - Skate Anthem",
                    likesCount = 450,
                    viewsCount = 1201,
                    hashtags = "#skateboarding #skatelife #extremesports #training"
                ),
                ShortVideo(
                    id = 3,
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-waves-breaking-in-the-ocean-1527-large.mp4",
                    creatorName = "@ocean_breeze",
                    avatarUrl = "OB",
                    description = "Chasing waves and feeling the cool salty wind. Nature is therapeutic 🌊💙 Take a deep breath. #oceanlife #waveporn #seaside #peaceful",
                    musicTrack = "Ambient Waves in G Minor",
                    likesCount = 3200,
                    viewsCount = 18450,
                    hashtags = "#oceanlife #waveporn #seaside #peaceful"
                ),
                ShortVideo(
                    id = 4,
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-stars-in-the-night-sky-11881-large.mp4",
                    creatorName = "@cosmic_voyager",
                    avatarUrl = "CV",
                    description = "Deep space stargazing observatory. The universe is full of secrets wait to be found 🌌🌏 #astrophotography #space #cosmos #nebula",
                    musicTrack = "Interstellar Synth Solo",
                    likesCount = 5200,
                    viewsCount = 34000,
                    hashtags = "#astrophotography #space #cosmos #nebula"
                ),
                ShortVideo(
                    id = 5,
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4",
                    creatorName = "@outdoors_spirit",
                    avatarUrl = "OS",
                    description = "A hidden serene forest stream under bright morning rays. Refreshing walk in the woods.🍃✨ #naturelovers #camping #serene #forest",
                    musicTrack = "Nature Birds Morning Flute",
                    likesCount = 980,
                    viewsCount = 5600,
                    hashtags = "#naturelovers #camping #serene #forest"
                )
            )
            videoDao.insertVideos(initialVideos)

            // Seed Comments for video 1
            videoDao.insertComment(VideoComment(videoId = 1, username = "@rose_gold", text = "This aesthetic is unmatched! Where is this bookstore? 💜"))
            videoDao.insertComment(VideoComment(videoId = 1, username = "@lofi_lover", text = "Perfect chill vibes for study sessions! I love Lofi music."))
            videoDao.insertComment(VideoComment(videoId = 1, username = "@dreamer_blue", text = "Adding this book to my late reading playlist! Name?"))
            videoDao.insertComment(VideoComment(videoId = 1, username = "@novel_nest", text = "The book choice is excellent! Great content."))

            // Seed Comments for video 2
            videoDao.insertComment(VideoComment(videoId = 2, username = "@tony_stunt", text = "Bro that flips was absolutely clean! 🛹✨"))
            videoDao.insertComment(VideoComment(videoId = 2, username = "@mom_safety", text = "Please wear your helmet and kneepads! Love mom."))

            // Seed Comments for video 3
            videoDao.insertComment(VideoComment(videoId = 3, username = "@salt_life", text = "Nothing compares to the power of the ocean 🌊"))
            videoDao.insertComment(VideoComment(videoId = 3, username = "@meditate_now", text = "I am using this loop to breathe. In and out... thank you!"))
        }
    }
}
