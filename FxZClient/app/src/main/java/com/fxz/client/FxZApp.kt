package com.fxz.client

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.fxz.client.utils.AntiCrash
import com.fxz.client.utils.Constants
import com.fxz.client.utils.PerformanceManager
import com.fxz.client.utils.ThemeManager
import javax.inject.Inject

@HiltAndroidApp
class FxZApp : Application(), Configuration.Provider {

    @Inject lateinit var themeManager: ThemeManager
    @Inject lateinit var performanceManager: PerformanceManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupStrictMode()
        AntiCrash.init(this)
        createNotificationChannels()
        themeManager.applyTheme()
        performanceManager.initOptimizations()
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channels = listOf(
                NotificationChannel(
                    Constants.CHANNEL_DOWNLOAD,
                    "Download Progress",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "FxZ Client file download progress" },
                NotificationChannel(
                    Constants.CHANNEL_GAME,
                    "Game Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "SA:MP game session status" },
                NotificationChannel(
                    Constants.CHANNEL_UPDATE,
                    "Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Client update notifications" },
                NotificationChannel(
                    Constants.CHANNEL_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "General notifications" }
            )
            channels.forEach { nm.createNotificationChannel(it) }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    companion object {
        lateinit var instance: FxZApp
            private set
    }
}
