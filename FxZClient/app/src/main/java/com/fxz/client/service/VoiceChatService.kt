package com.fxz.client.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.fxz.client.R
import com.fxz.client.ui.main.MainActivity
import com.fxz.client.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

@AndroidEntryPoint
class VoiceChatService : LifecycleService() {

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private var socket: DatagramSocket? = null
    private var serverIp: String = ""
    private var voicePort: Int = 7778

    companion object {
        const val SAMPLE_RATE = 16000
        const val BUFFER_SIZE_MULTIPLIER = 4
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        ) * BUFFER_SIZE_MULTIPLIER
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(Constants.NOTIF_OVERLAY + 1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        serverIp = intent?.getStringExtra(Constants.EXTRA_SERVER_IP) ?: return START_NOT_STICKY
        voicePort = intent.getIntExtra("voice_port", 7778)
        startCapture()
        return START_STICKY
    }

    private fun startCapture() {
        captureJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize
                )
                socket = DatagramSocket()
                val addr = InetAddress.getByName(serverIp)
                audioRecord?.startRecording()
                val buf = ByteArray(bufferSize)
                while (isActive) {
                    val read = audioRecord?.read(buf, 0, bufferSize) ?: break
                    if (read > 0) {
                        val pkt = DatagramPacket(buf, read, addr, voicePort)
                        runCatching { socket?.send(pkt) }
                    }
                }
            } finally {
                audioRecord?.stop(); audioRecord?.release(); audioRecord = null
                socket?.close(); socket = null
            }
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, Constants.CHANNEL_GAME)
            .setContentTitle("FxZ Voice Chat Active")
            .setContentText("Microphone in use")
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        captureJob?.cancel()
        super.onDestroy()
    }
}
