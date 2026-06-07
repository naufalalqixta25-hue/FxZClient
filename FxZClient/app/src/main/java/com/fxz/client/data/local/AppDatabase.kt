package com.fxz.client.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fxz.client.data.local.dao.DownloadDao
import com.fxz.client.data.local.dao.PlayerProfileDao
import com.fxz.client.data.local.dao.ServerDao
import com.fxz.client.data.model.DownloadTask
import com.fxz.client.data.model.PlayerProfile
import com.fxz.client.data.model.Server

@Database(
    entities = [Server::class, PlayerProfile::class, DownloadTask::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun profileDao(): PlayerProfileDao
    abstract fun downloadDao(): DownloadDao
}
