package com.fxz.client.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.data.model.GameConfig
import com.fxz.client.repository.GameRepository
import com.fxz.client.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SharedPreferences,
    private val themeManager: ThemeManager,
    private val performanceManager: PerformanceManager,
    private val cloudBackup: CloudBackup,
    private val gameRepository: GameRepository,
    private val deviceInfo: DeviceInfo
) : ViewModel() {

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<GameConfig> = _config

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage

    val themes = themeManager.getThemeList()
    val fpsOptions = Constants.FPS_OPTIONS
    val deviceModel = deviceInfo.getDeviceModel()
    val androidVersion = deviceInfo.getAndroidVersion()
    val cpuAbi = performanceManager.getCpuAbi()

    private fun loadConfig() = GameConfig(
        fpsLock         = prefs.getInt(Constants.PREF_FPS_LOCK, 60),
        sensitivity     = prefs.getFloat(Constants.PREF_SENSITIVITY, 1.0f),
        antiLag         = prefs.getBoolean(Constants.PREF_ANTI_LAG, true),
        gpuOptimization = prefs.getBoolean(Constants.PREF_GPU_OPT, true),
        performanceMode = prefs.getBoolean(Constants.PREF_PERF_MODE, false),
        batterySaverMode= prefs.getBoolean(Constants.PREF_BATTERY_SAVER, false),
        showFpsOverlay  = prefs.getBoolean(Constants.PREF_SHOW_OVERLAY, true),
        crosshairType   = prefs.getInt(Constants.PREF_CROSSHAIR, 0),
        voiceChat       = prefs.getBoolean(Constants.PREF_VOICE_CHAT, false),
        audioEnhancer   = prefs.getBoolean(Constants.PREF_AUDIO_ENHANCER, false),
        theme           = prefs.getString(Constants.PREF_THEME, Constants.THEME_NEON_BLUE) ?: Constants.THEME_NEON_BLUE
    )

    fun updateFpsLock(fps: Int) {
        prefs.edit().putInt(Constants.PREF_FPS_LOCK, fps).apply()
        _config.value = _config.value.copy(fpsLock = fps)
    }

    fun updateSensitivity(v: Float) {
        prefs.edit().putFloat(Constants.PREF_SENSITIVITY, v).apply()
        _config.value = _config.value.copy(sensitivity = v)
    }

    fun updateAntiLag(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_ANTI_LAG, v).apply()
        _config.value = _config.value.copy(antiLag = v)
    }

    fun updateGpuOpt(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_GPU_OPT, v).apply()
        _config.value = _config.value.copy(gpuOptimization = v)
    }

    fun updatePerformanceMode(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_PERF_MODE, v).apply()
        _config.value = _config.value.copy(performanceMode = v)
        performanceManager.applyPerformanceMode(v)
    }

    fun updateBatterySaver(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_BATTERY_SAVER, v).apply()
        _config.value = _config.value.copy(batterySaverMode = v)
        performanceManager.applyBatterySaverMode(v)
    }

    fun updateOverlay(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_SHOW_OVERLAY, v).apply()
        _config.value = _config.value.copy(showFpsOverlay = v)
    }

    fun updateVoiceChat(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_VOICE_CHAT, v).apply()
        _config.value = _config.value.copy(voiceChat = v)
    }

    fun updateAudioEnhancer(v: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_AUDIO_ENHANCER, v).apply()
        _config.value = _config.value.copy(audioEnhancer = v)
    }

    fun setTheme(themeId: String) {
        themeManager.setTheme(themeId)
        prefs.edit().putString(Constants.PREF_THEME, themeId).apply()
        _config.value = _config.value.copy(theme = themeId)
    }

    fun backupConfig() {
        viewModelScope.launch {
            val ok = cloudBackup.backupConfig(_config.value)
            _uiMessage.emit(if (ok) "Config backed up ✓" else "Backup failed")
        }
    }

    fun restoreConfig() {
        viewModelScope.launch {
            val restored = cloudBackup.restoreConfig()
            if (restored != null) {
                _config.value = restored
                _uiMessage.emit("Config restored ✓")
            } else {
                _uiMessage.emit("No backup found")
            }
        }
    }

    fun cleanCache() {
        viewModelScope.launch {
            gameRepository.cleanCache()
            _uiMessage.emit("Cache cleared ✓")
        }
    }

    fun getDeviceScore(): Int = performanceManager.getDeviceScore()
    fun getRecommendedFps(): Int = performanceManager.getRecommendedFps()
}
