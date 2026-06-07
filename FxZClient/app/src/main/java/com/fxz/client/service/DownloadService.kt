package com.fxz.client.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.fxz.client.R
import com.fxz.client.data.model.DownloadProgress
import com.fxz.client.data.model.DownloadStatus
import com.fxz.client.data.model.DownloadTask
import com.fxz.client.repository.DownloadRepository
import com.fxz.client.ui.main.MainActivity
import com.fxz.client.utils.Constants
import com.fxz.client.utils.StorageUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : LifecycleService() {

    @Inject lateinit var downloadRepository: DownloadRepository
    @Inject lateinit var storageUtils: StorageUtils
    @Inject lateinit var okHttpClient: OkHttpClient

    private val activeDownloads = mutableMapOf<String, Job>()
    private var downloadJob: Job? = null

    companion object {
        private const val TAG = "FxZ-Download"
        const val EXTRA_TASK_ID = "task_id"
        const val ACTION_START  = "action_start"
        const val ACTION_PAUSE  = "action_pause"
        const val ACTION_CANCEL = "action_cancel"
        val progressFlow = kotlinx.coroutines.flow.MutableSharedFlow<DownloadProgress>(
            extraBufferCapacity = 32
        )
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(Constants.NOTIF_DOWNLOAD, buildNotification("Ready", 0))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START  -> intent.getStringExtra(EXTRA_TASK_ID)?.let { startDownload(it) }
            ACTION_PAUSE  -> intent.getStringExtra(EXTRA_TASK_ID)?.let { pauseDownload(it) }
            ACTION_CANCEL -> intent.getStringExtra(EXTRA_TASK_ID)?.let { cancelDownload(it) }
        }
        return START_STICKY
    }

    private fun startDownload(taskId: String) {
        if (activeDownloads.containsKey(taskId)) return
        val job = lifecycleScope.launch(Dispatchers.IO) {
            val task = downloadRepository.getDownload(taskId) ?: return@launch
            performDownload(task)
        }
        activeDownloads[taskId] = job
    }

    private fun pauseDownload(taskId: String) {
        activeDownloads[taskId]?.cancel()
        activeDownloads.remove(taskId)
        lifecycleScope.launch {
            downloadRepository.updateStatus(taskId, DownloadStatus.PAUSED)
        }
    }

    private fun cancelDownload(taskId: String) {
        activeDownloads[taskId]?.cancel()
        activeDownloads.remove(taskId)
        lifecycleScope.launch {
            downloadRepository.updateStatus(taskId, DownloadStatus.CANCELLED)
        }
    }

    private suspend fun performDownload(task: DownloadTask) {
        val destFile = File(task.destPath)
        destFile.parentFile?.mkdirs()

        try {
            downloadRepository.updateStatus(task.id, DownloadStatus.DOWNLOADING)

            // Resume support: check existing bytes
            val resumeFrom = if (destFile.exists()) destFile.length() else 0L
            val request = Request.Builder()
                .url(task.url)
                .apply { if (resumeFrom > 0) addHeader("Range", "bytes=$resumeFrom-") }
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body ?: throw Exception("Empty response")
            val contentLength = body.contentLength()
            val totalBytes = if (resumeFrom > 0 && contentLength > 0)
                resumeFrom + contentLength else contentLength

            var downloaded = resumeFrom
            var lastSpeedCheck = System.currentTimeMillis()
            var lastBytes = downloaded

            val out = if (resumeFrom > 0) RandomAccessFile(destFile, "rw").also {
                it.seek(resumeFrom)
            } else null

            val fos = if (out == null) FileOutputStream(destFile) else null

            try {
                body.byteStream().use { input ->
                    val buf = ByteArray(65536)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) {
                        if (!coroutineContext.isActive) break
                        out?.write(buf, 0, n) ?: fos?.write(buf, 0, n)
                        downloaded += n

                        val now = System.currentTimeMillis()
                        if (now - lastSpeedCheck >= 1000) {
                            val speed = downloaded - lastBytes
                            val progress = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 0
                            lastSpeedCheck = now; lastBytes = downloaded
                            val dp = DownloadProgress(task.id, downloaded, totalBytes, speed, progress, DownloadStatus.DOWNLOADING)
                            progressFlow.tryEmit(dp)
                            downloadRepository.updateProgress(task.id, downloaded, progress, speed, DownloadStatus.DOWNLOADING)
                            updateNotification("Downloading ${task.name}", progress)
                        }
                    }
                }
            } finally {
                out?.close(); fos?.close()
            }

            if (coroutineContext.isActive) {
                downloadRepository.updateStatus(task.id, DownloadStatus.COMPLETED)
                progressFlow.tryEmit(DownloadProgress(task.id, totalBytes, totalBytes, 0, 100, DownloadStatus.COMPLETED))
                Log.i(TAG, "Download complete: ${task.name}")
            }

        } catch (e: CancellationException) {
            Log.d(TAG, "Download cancelled: ${task.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${task.name}", e)
            downloadRepository.updateStatus(task.id, DownloadStatus.FAILED, e.message)
            progressFlow.tryEmit(DownloadProgress(task.id, 0, 0, 0, 0, DownloadStatus.FAILED, e.message))
        } finally {
            activeDownloads.remove(task.id)
        }
    }

    private fun buildNotification(text: String, progress: Int): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, Constants.CHANNEL_DOWNLOAD)
            .setContentTitle("FxZ Client")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    private fun updateNotification(text: String, progress: Int) {
        val nm = getSystemService(android.app.NotificationManager::class.java)
        nm.notify(Constants.NOTIF_DOWNLOAD, buildNotification(text, progress))
    }

    override fun onDestroy() {
        activeDownloads.values.forEach { it.cancel() }
        activeDownloads.clear()
        super.onDestroy()
    }
}
