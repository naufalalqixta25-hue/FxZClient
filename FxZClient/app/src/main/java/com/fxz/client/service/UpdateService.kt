package com.fxz.client.service

import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.fxz.client.R
import com.fxz.client.repository.UpdateRepository
import com.fxz.client.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UpdateService : LifecycleService() {

    @Inject lateinit var updateRepository: UpdateRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        lifecycleScope.launch {
            val info = updateRepository.getUpdateInfo()
            if (info != null) {
                showUpdateNotification(info.latestVersion, info.changelog)
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showUpdateNotification(version: String, changelog: String) {
        val notif = NotificationCompat.Builder(this, Constants.CHANNEL_UPDATE)
            .setSmallIcon(R.drawable.ic_fxz_logo)
            .setContentTitle("FxZ Client $version Available")
            .setContentText(changelog.take(80))
            .setStyle(NotificationCompat.BigTextStyle().bigText(changelog))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching {
            NotificationManagerCompat.from(this).notify(Constants.NOTIF_UPDATE, notif)
        }
    }
}
