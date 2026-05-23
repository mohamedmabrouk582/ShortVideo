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
            val videoUrls = listOf(
                "https://assets.mixkit.co/videos/preview/mixkit-girl-in-neon-sign-sitting-on-ground-reading-40345-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-skater-doing-a-trick-in-a-skatepark-42232-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-waves-breaking-in-the-ocean-1527-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-stars-in-the-night-sky-11881-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-coffee-pouring-into-a-glass-cup-42200-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-hands-of-a-gamer-playing-with-a-controller-41618-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-chef-plating-a-gourmet-dish-42510-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-skater-doing-a-trick-in-a-skatepark-42045-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-play-of-sunlight-on-a-forest-floor-after-rain-41584-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-person-holding-sparklers-at-dusk-42171-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-woman-exercising-on-yoga-mat-42354-large.mp4"
            )

            val creators = listOf(
                "@neon_reader" to "NR",
                "@skate_pro" to "SP",
                "@ocean_breeze" to "OB",
                "@cosmic_voyager" to "CV",
                "@outdoors_spirit" to "OS",
                "@cafe_lofi" to "CL",
                "@pixel_gamer" to "PG",
                "@gourmet_chef" to "GC",
                "@skate_jump" to "SJ",
                "@forest_walk" to "FW",
                "@sparkler_dusk" to "SD",
                "@yoga_zen" to "YZ"
            )

            val descriptions = listOf(
                "Midnight sessions are magical... 📖✨ Best place to get lost in a story." to "#aesthetic #lofivibe #reading",
                "Nailing this new flip trick on a sunny afternoon! 🛹🔥 Persistence pays off." to "#skateboarding #skatelife #extremesport",
                "Chasing waves and feeling the cool salty wind. Nature is therapeutic 🌊💙 Take a deep breath." to "#oceanlife #seaside #peaceful",
                "Deep space stargazing observatory. The universe is full of secrets waiting to be found 🌌🌏" to "#astrophoto #space #cosmos",
                "A hidden serene forest stream under bright morning rays. Refreshing walk in the woods.🍃✨" to "#naturelover #serene #forest",
                "Fresh aromatic coffee morning routine. The smell of freshly brewed beans is unparalleled.☕️🤎" to "#coffeetime #cafevibes #morning",
                "An intense focus gaming session with the latest immersive controllers. 🎮🕹️" to "#gamers #rgbconfig #gamingstation",
                "Dedicating absolute focus on plating this beautiful delicate gourmet dish. Bon appétit! 🍽️✨" to "#cooking #foodie #chefslife",
                "Incredible skateboard launch in high motion. Skating is an art and a lifestyle. 🛹🌟" to "#skatepark #skaterjump #thrills",
                "The play of bright golden sunlight on wet leaves after a heavy morning shower. 🌿💧" to "#freshrain #naturelovers #outdoor",
                "Celebrating warm magical moments with friends lighting sparklers at dusk. 🎇✨" to "#sparklers #duskmoments #joy",
                "Starting the day right with stretching and deep meditative breathing. Mind and body alignment.🧘‍♀️🌸" to "#yoga #meditation #wellness"
            )

            val musicTracks = listOf(
                "Lofi Chills - Sunset Vibes",
                "Hyperactive Punk Rock - Skate Anthem",
                "Ambient Waves in G Minor",
                "Interstellar Synth Solo",
                "Nature Birds Morning Flute",
                "Espresso Jazz Cafe Beats",
                "Cyber Neon Electro Chase",
                "Classic Piano Elegance",
                "Upbeat Ska Rhythms",
                "Gentle Whispering Pine Winds",
                "Summer Campfire Acoustic Guitar",
                "Peaceful Tibetan Singing Bowl"
            )

            val seededVideos = mutableListOf<ShortVideo>()

            for (i in 1..100) {
                val indexMod = (i - 1) % videoUrls.size
                val creatorInfo = creators[indexMod]
                val descInfo = descriptions[indexMod]
                val musicTrackName = musicTracks[indexMod]

                val uniqueCreator = "${creatorInfo.first}_$i"
                val finalDescription = "Part $i: ${descInfo.first} ${descInfo.second}"
                val likes = (i * 263 + 124) % 15800 + 40
                val views = likes * 4 + (i * 12 + 80)

                seededVideos.add(
                    ShortVideo(
                        id = i.toLong(),
                        videoUrl = videoUrls[indexMod],
                        creatorName = uniqueCreator,
                        avatarUrl = creatorInfo.second,
                        description = finalDescription,
                        musicTrack = "$musicTrackName (Seeded Part $i)",
                        likesCount = likes,
                        viewsCount = views,
                        hashtags = descInfo.second
                    )
                )
            }

            videoDao.insertVideos(seededVideos)

            // Seed generic comments on first 5 videos to keep interface active
            for (vId in 1L..5L) {
                videoDao.insertComment(VideoComment(videoId = vId, username = "@study_zen", text = "This visual is so relaxing, watched it multiple times! 🙌"))
                videoDao.insertComment(VideoComment(videoId = vId, username = "@viewer_pro", text = "This content quality is amazing! Seeded video #$vId is super stable."))
                videoDao.insertComment(VideoComment(videoId = vId, username = "@active_short", text = "Is there a full length track list anywhere? Sounds spectacular! 🎵"))
            }
        }
    }
}
