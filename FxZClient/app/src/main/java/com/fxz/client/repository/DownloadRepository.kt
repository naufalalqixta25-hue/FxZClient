package com.fxz.client.repository

import com.fxz.client.data.local.dao.DownloadDao
import com.fxz.client.data.model.DownloadStatus
import com.fxz.client.data.model.DownloadTask
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(private val downloadDao: DownloadDao) {
    fun getAllDownloads(): Flow<List<DownloadTask>> = downloadDao.getAllDownloads()
    suspend fun getDownload(id: String): DownloadTask? = downloadDao.getDownload(id)
    suspend fun insertTask(task: DownloadTask) = downloadDao.insert(task)
    suspend fun updateStatus(id: String, status: DownloadStatus, error: String? = null) =
        downloadDao.updateStatus(id, status, error)
    suspend fun updateProgress(id: String, bytes: Long, progress: Int, speed: Long, status: DownloadStatus) =
        downloadDao.updateProgress(id, bytes, progress, speed, status)
    suspend fun deleteTask(task: DownloadTask) = downloadDao.delete(task)
    suspend fun clearCompleted() = downloadDao.clearCompleted()
    suspend fun getActiveDownloads(): List<DownloadTask> = downloadDao.getActiveDownloads()
}
