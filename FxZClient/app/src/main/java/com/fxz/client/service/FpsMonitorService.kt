package com.fxz.client.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.fxz.client.utils.RamCleaner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@AndroidEntryPoint
class FpsMonitorService : Service() {

    @Inject lateinit var ramCleaner: RamCleaner

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        private val _fps = MutableStateFlow(0)
        private val _ram = MutableStateFlow(0)
        val fps: StateFlow<Int> = _fps
        val ram: StateFlow<Int> = _ram

        private var frameCount = 0
        private var lastCheck  = System.currentTimeMillis()

        fun onFrame() { frameCount++ }
    }

    private var monitorJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        monitorJob = scope.launch {
            while (isActive) {
                delay(1000)
                val now    = System.currentTimeMillis()
                val delta  = (now - lastCheck).coerceAtLeast(1)
                _fps.value = (frameCount * 1000L / delta).toInt().coerceIn(0, 999)
                _ram.value = ramCleaner.getRamUsagePercent()
                frameCount = 0
                lastCheck  = now
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
