package com.fxz.client.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ramCleaner: RamCleaner
) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun initOptimizations() {
        setProcessPriority()
    }

    private fun setProcessPriority() {
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND)
        } catch (_: Exception) {}
    }

    fun applyPerformanceMode(enabled: Boolean) {
        if (enabled) {
            System.gc()
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
        } else {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        }
    }

    fun applyBatterySaverMode(enabled: Boolean) {
        // Reduce background operations when battery saver is active
        if (enabled) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
        }
    }

    fun getDeviceScore(): Int {
        val totalRam = ramCleaner.getTotalRam()
        val ramScore = when {
            totalRam >= 8_000_000_000L -> 100
            totalRam >= 6_000_000_000L -> 85
            totalRam >= 4_000_000_000L -> 70
            totalRam >= 3_000_000_000L -> 55
            totalRam >= 2_000_000_000L -> 40
            else                       -> 25
        }
        return ramScore
    }

    fun getRecommendedFps(): Int {
        val score = getDeviceScore()
        return when {
            score >= 85 -> 90
            score >= 70 -> 60
            score >= 55 -> 45
            else        -> 30
        }
    }

    fun getCpuAbi(): String = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"

    fun isSnapdragon(): Boolean = Build.HARDWARE.contains("qcom", true) ||
        (Build.SOC_MODEL.contains("Snapdragon", true))

    fun isMediaTek(): Boolean = Build.HARDWARE.contains("mt", true) ||
        (Build.SOC_MANUFACTURER.contains("MediaTek", true))

    fun getOptimalThreadCount(): Int = Runtime.getRuntime().availableProcessors()
        .coerceIn(2, 8)
}
