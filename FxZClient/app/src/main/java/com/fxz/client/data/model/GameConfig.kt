package com.fxz.client.data.model

data class GameConfig(
    val fpsLock: Int = 60,
    val sensitivity: Float = 1.0f,
    val antiLag: Boolean = true,
    val gpuOptimization: Boolean = true,
    val performanceMode: Boolean = false,
    val batterySaverMode: Boolean = false,
    val showFpsOverlay: Boolean = true,
    val showPingOverlay: Boolean = true,
    val showHud: Boolean = true,
    val crosshairType: Int = 0,
    val voiceChat: Boolean = false,
    val audioEnhancer: Boolean = false,
    val theme: String = "neon_blue",
    val customFont: String = "default",
    val touchControlLayout: String = "default"
)

data class AppUpdateInfo(
    val latestVersion: String,
    val versionCode: Int,
    val downloadUrl: String,
    val changelog: String,
    val isMandatory: Boolean,
    val releaseDate: String
)

data class NewsItem(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String,
    val link: String,
    val date: Long,
    val type: String
)

data class DeviceStats(
    val cpuUsage: Float,
    val ramUsage: Float,
    val ramTotal: Long,
    val ramAvailable: Long,
    val temperature: Float,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val networkType: String,
    val gpuRenderer: String
)
