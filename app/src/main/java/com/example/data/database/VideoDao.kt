package com.example.data.database

import androidx.room.*
import com.example.data.model.ShortVideo
import com.example.data.model.VideoComment
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM short_videos ORDER BY id DESC")
    fun getAllVideos(): Flow<List<ShortVideo>>

    @Query("SELECT * FROM short_videos WHERE id = :id")
    suspend fun getVideoById(id: Long): ShortVideo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: ShortVideo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideos(videos: List<ShortVideo>)

    @Update
    suspend fun updateVideo(video: ShortVideo)

    @Query("DELETE FROM short_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("SELECT * FROM video_comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: Long): Flow<List<VideoComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: VideoComment): Long

    @Query("DELETE FROM video_comments WHERE commentId = :commentId")
    suspend fun deleteComment(commentId: Long)
}
