package com.fxz.client.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import com.fxz.client.data.model.DeviceStats
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ramCleaner: RamCleaner
) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getDeviceStats(): DeviceStats {
        val batteryIntent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val batteryLevel = batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level != -1 && scale != -1) (level * 100 / scale) else -1
        } ?: -1
        val isCharging = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS, -1
        ) == BatteryManager.BATTERY_STATUS_CHARGING

        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

        return DeviceStats(
            cpuUsage      = getCpuUsage(),
            ramUsage      = (memInfo.totalMem - memInfo.availMem).toFloat(),
            ramTotal      = memInfo.totalMem,
            ramAvailable  = memInfo.availMem,
            temperature   = 0f,
            batteryLevel  = batteryLevel,
            isCharging    = isCharging,
            networkType   = getNetworkType(),
            gpuRenderer   = getGpuInfo()
        )
    }

    private fun getCpuUsage(): Float {
        return try {
            val stat = java.io.File("/proc/stat").readLines().firstOrNull() ?: return 0f
            val parts = stat.split("\\s+".toRegex()).drop(1)
            val total = parts.take(7).sumOf { it.toLong() }
            val idle  = parts[3].toLong()
            (1.0f - idle.toFloat() / total) * 100
        } catch (_: Exception) { 0f }
    }

    private fun getNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val net = cm.activeNetwork ?: return "None"
        val caps = cm.getNetworkCapabilities(net) ?: return "None"
        return when {
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)     -> "WiFi"
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }

    private fun getGpuInfo(): String = Build.HARDWARE

    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    fun getSupportedAbis(): List<String> = Build.SUPPORTED_ABIS.toList()
    fun isArm64(): Boolean = Build.SUPPORTED_64_BIT_ABIS.contains("arm64-v8a")
}
