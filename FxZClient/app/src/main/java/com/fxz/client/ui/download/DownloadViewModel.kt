package com.fxz.client.ui.download

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.data.model.DownloadStatus
import com.fxz.client.data.model.DownloadTask
import com.fxz.client.repository.DownloadRepository
import com.fxz.client.repository.GameRepository
import com.fxz.client.service.DownloadService
import com.fxz.client.utils.Constants
import com.fxz.client.utils.StorageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository,
    private val gameRepository: GameRepository,
    private val storageUtils: StorageUtils
) : ViewModel() {

    val downloads: StateFlow<List<DownloadTask>> =
        downloadRepository.getAllDownloads()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGameReady = MutableStateFlow(false)
    val isGameReady: StateFlow<Boolean> = _isGameReady

    private val _totalProgress = MutableStateFlow(0)
    val totalProgress: StateFlow<Int> = _totalProgress

    private val _statusText = MutableStateFlow("Checking game files…")
    val statusText: StateFlow<String> = _statusText

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage

    private val _phase = MutableStateFlow(SetupPhase.CHECKING)
    val phase: StateFlow<SetupPhase> = _phase

    init {
        checkSetup()
        observeProgress()
    }

    private fun checkSetup() {
        viewModelScope.launch {
            _isGameReady.value = gameRepository.isGameSetupComplete()
            _phase.value = if (_isGameReady.value) SetupPhase.READY else SetupPhase.CHECKING
            _statusText.value = if (_isGameReady.value)
                "Game is ready to play!" else "Game files not found. Download required."
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            DownloadService.progressFlow.collect { dp ->
                downloadRepository.updateProgress(dp.taskId, dp.downloaded, dp.progress, dp.speed, dp.status)
                if (dp.status == DownloadStatus.COMPLETED) {
                    checkNextTask()
                }
            }
        }
    }

    fun startFullSetup() {
        viewModelScope.launch {
            gameRepository.ensureDirectories()
            val freeSpace = storageUtils.getFreeSpace(storageUtils.getOptimalStoragePath())
            if (freeSpace < 3_000_000_000L) {
                _uiMessage.emit("Need at least 3GB free space. Available: ${storageUtils.formatBytes(freeSpace)}")
                return@launch
            }
            _phase.value = SetupPhase.DOWNLOADING
            _statusText.value = "Starting download…"
            enqueueGtaSaDownload()
            enqueueSampDownload()
        }
    }

    private suspend fun enqueueGtaSaDownload() {
        val task = DownloadTask(
            id       = "gtasa_data",
            name     = "GTA San Andreas Data",
            url      = Constants.CDN_GTASA_DATA,
            destPath = "${gameRepository.getGameDataPath()}/data.zip",
            totalBytes = Constants.GTASA_DATA_SIZE
        )
        downloadRepository.insertTask(task)
        startDownloadService("gtasa_data")
    }

    private suspend fun enqueueSampDownload() {
        val task = DownloadTask(
            id       = "samp_lib",
            name     = "SA:MP Library",
            url      = Constants.CDN_SAMP_LIB,
            destPath = "${gameRepository.getSampPath()}/${Constants.SAMP_LIB}",
            totalBytes = Constants.SAMP_LIB_SIZE
        )
        downloadRepository.insertTask(task)
        startDownloadService("samp_lib")
    }

    private fun startDownloadService(taskId: String) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_START
            putExtra(DownloadService.EXTRA_TASK_ID, taskId)
        }
        context.startForegroundService(intent)
    }

    fun pauseDownload(taskId: String) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_PAUSE
            putExtra(DownloadService.EXTRA_TASK_ID, taskId)
        }
        context.startService(intent)
    }

    fun resumeDownload(taskId: String) = startDownloadService(taskId)

    fun cancelDownload(taskId: String) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_CANCEL
            putExtra(DownloadService.EXTRA_TASK_ID, taskId)
        }
        context.startService(intent)
    }

    private fun checkNextTask() {
        viewModelScope.launch {
            val active = downloadRepository.getActiveDownloads()
            if (active.isEmpty()) {
                val ready = gameRepository.isGameSetupComplete()
                _isGameReady.value = ready
                _phase.value = if (ready) SetupPhase.READY else SetupPhase.EXTRACTING
                _statusText.value = if (ready) "All done! Game ready." else "Extracting files…"
            }
        }
    }

    fun getStorageInfo(): String {
        val path  = storageUtils.getOptimalStoragePath()
        val free  = storageUtils.formatBytes(storageUtils.getFreeSpace(path))
        val total = storageUtils.formatBytes(storageUtils.getTotalSpace(path))
        return "Storage: $free free / $total"
    }

    fun clearCompleted() {
        viewModelScope.launch { downloadRepository.clearCompleted() }
    }

    enum class SetupPhase { CHECKING, DOWNLOADING, EXTRACTING, READY }
}
