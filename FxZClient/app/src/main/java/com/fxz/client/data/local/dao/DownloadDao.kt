package com.fxz.client.data.local.dao

import androidx.room.*
import com.fxz.client.data.model.DownloadStatus
import com.fxz.client.data.model.DownloadTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownload(id: String): DownloadTask?

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING','DOWNLOADING','PAUSED')")
    suspend fun getActiveDownloads(): List<DownloadTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DownloadTask)

    @Update
    suspend fun update(task: DownloadTask)

    @Delete
    suspend fun delete(task: DownloadTask)

    @Query("UPDATE downloads SET downloadedBytes = :bytes, progress = :progress, speed = :speed, status = :status WHERE id = :id")
    suspend fun updateProgress(id: String, bytes: Long, progress: Int, speed: Long, status: DownloadStatus)

    @Query("UPDATE downloads SET status = :status, error = :error WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus, error: String? = null)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED' OR status = 'CANCELLED'")
    suspend fun clearCompleted()
}
