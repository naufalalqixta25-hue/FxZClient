package com.fxz.client.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.fxz.client.R
import com.fxz.client.ui.main.MainActivity
import com.fxz.client.utils.Constants
import com.fxz.client.utils.RamCleaner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class InGameOverlayService : LifecycleService() {

    @Inject lateinit var ramCleaner: RamCleaner

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private var fpsMonitorJob: Job? = null
    private var lastFrameTime = System.currentTimeMillis()
    private var frameCount = 0

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(Constants.NOTIF_OVERLAY, buildNotification())
        addOverlay()
        startFpsMonitor()
    }

    private fun addOverlay() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_ingame, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16; y = 60
        }
        makeDraggable(view, params)
        wm.addView(view, params)
        overlayView = view
    }

    private fun makeDraggable(view: View, params: WindowManager.LayoutParams) {
        var startX = 0f; var startY = 0f
        var initX = 0; var initY = 0
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { startX = event.rawX; startY = event.rawY; initX = params.x; initY = params.y; true }
                MotionEvent.ACTION_MOVE -> { params.x = initX + (event.rawX - startX).toInt(); params.y = initY + (event.rawY - startY).toInt(); wm.updateViewLayout(view, params); true }
                else -> false
            }
        }
    }

    private fun startFpsMonitor() {
        fpsMonitorJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000)
                val now = System.currentTimeMillis()
                val fps = (frameCount * 1000L / (now - lastFrameTime)).toInt().coerceIn(0, 999)
                lastFrameTime = now; frameCount = 0
                val ram = ramCleaner.getRamUsagePercent()
                withContext(Dispatchers.Main) { updateOverlay(fps, ram) }
            }
        }
    }

    private fun updateOverlay(fps: Int, ram: Int) {
        overlayView?.let {
            it.findViewById<TextView>(R.id.tvFps)?.text = "FPS: $fps"
            it.findViewById<TextView>(R.id.tvRam)?.text = "RAM: $ram%"
        }
    }

    fun tickFrame() { frameCount++ }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, Constants.CHANNEL_GAME)
            .setContentTitle("FxZ Client – In Game")
            .setContentText("Overlay active")
            .setSmallIcon(R.drawable.ic_fxz_logo)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == Constants.ACTION_OVERLAY_HIDE) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        fpsMonitorJob?.cancel()
        overlayView?.let { runCatching { wm.removeView(it) } }
        super.onDestroy()
    }
}
