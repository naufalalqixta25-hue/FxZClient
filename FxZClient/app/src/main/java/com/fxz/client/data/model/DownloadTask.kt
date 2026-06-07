package com.fxz.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadTask(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val destPath: String,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,
    val speed: Long = 0,
    val error: String? = null,
    val md5: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0
) {
    val progressFloat: Float get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
    val isPending: Boolean get() = status == DownloadStatus.PENDING
    val isRunning: Boolean get() = status == DownloadStatus.DOWNLOADING
    val isComplete: Boolean get() = status == DownloadStatus.COMPLETED
    val isFailed: Boolean get() = status == DownloadStatus.FAILED
}

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}

data class DownloadProgress(
    val taskId: String,
    val downloaded: Long,
    val total: Long,
    val speed: Long,
    val progress: Int,
    val status: DownloadStatus,
    val error: String? = null
)
