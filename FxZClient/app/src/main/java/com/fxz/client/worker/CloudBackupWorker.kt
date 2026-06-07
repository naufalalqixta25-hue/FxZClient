package com.fxz.client.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.fxz.client.data.model.GameConfig
import com.fxz.client.utils.CloudBackup
import com.fxz.client.utils.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CloudBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudBackup: CloudBackup,
    private val prefs: SharedPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val config = GameConfig(
                fpsLock         = prefs.getInt(Constants.PREF_FPS_LOCK, 60),
                sensitivity     = prefs.getFloat(Constants.PREF_SENSITIVITY, 1.0f),
                antiLag         = prefs.getBoolean(Constants.PREF_ANTI_LAG, true),
                gpuOptimization = prefs.getBoolean(Constants.PREF_GPU_OPT, true),
                performanceMode = prefs.getBoolean(Constants.PREF_PERF_MODE, false),
                batterySaverMode= prefs.getBoolean(Constants.PREF_BATTERY_SAVER, false),
                theme           = prefs.getString(Constants.PREF_THEME, Constants.THEME_NEON_BLUE) ?: Constants.THEME_NEON_BLUE
            )
            val ok = cloudBackup.backupConfig(config)
            if (ok) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun scheduleDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FxZ_CloudBackup",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
