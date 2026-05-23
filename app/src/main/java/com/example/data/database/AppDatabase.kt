package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.ShortVideo
import com.example.data.model.VideoComment

@Database(entities = [ShortVideo::class, VideoComment::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
