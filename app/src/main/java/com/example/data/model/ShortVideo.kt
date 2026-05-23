package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "short_videos")
data class ShortVideo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUrl: String,
    val creatorName: String,
    val avatarUrl: String, // Initial or mock URL
    val description: String,
    val musicTrack: String,
    val likesCount: Int = 0,
    val viewsCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val hashtags: String = "", // Space/Comma separated hashtags
    val filter: String = "None",
    val bgMusicUrl: String = "None",
    val bgMusicVolume: Float = 0.5f,
    val originalVideoVolume: Float = 1.0f
)
