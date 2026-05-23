package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_comments")
data class VideoComment(
    @PrimaryKey(autoGenerate = true) val commentId: Long = 0,
    val videoId: Long,
    val username: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
