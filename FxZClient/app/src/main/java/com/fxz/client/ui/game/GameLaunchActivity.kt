package com.fxz.client.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fxz.client.R
import com.fxz.client.databinding.ActivityGameLaunchBinding
import com.fxz.client.service.InGameOverlayService
import com.fxz.client.service.VoiceChatService
import com.fxz.client.utils.Constants
import com.fxz.client.utils.Extensions.toast
import com.fxz.client.utils.GameUtils
import com.fxz.client.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameLaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameLaunchBinding
    private val viewModel: GameLaunchViewModel by viewModels()

    @Inject lateinit var permissionUtils: PermissionUtils
    @Inject lateinit var gameUtils: GameUtils

    private var sessionStart = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityGameLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ip         = intent.getStringExtra(Constants.EXTRA_SERVER_IP) ?: ""
        val port       = intent.getIntExtra(Constants.EXTRA_SERVER_PORT, Constants.SAMP_DEFAULT_PORT)
        val serverName = intent.getStringExtra(Constants.EXTRA_SERVER_NAME) ?: ip
        val password   = intent.getStringExtra(Constants.EXTRA_PASSWORD) ?: ""
        val playerName = intent.getStringExtra("player_name") ?: ""

        binding.tvServerName.text = serverName
        binding.tvAddress.text    = "$ip:$port"
        binding.tvPlayerName.text = "Player: $playerName"
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnLaunch.setOnClickListener { launchGame(ip, port, playerName, password) }

        // Auto-launch after short delay
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1500)
            launchGame(ip, port, playerName, password)
        }
    }

    private fun launchGame(ip: String, port: Int, playerName: String, password: String) {
        if (ip.isEmpty() || playerName.isEmpty()) { toast("Missing info"); return }
        lifecycleScope.launch {
            binding.tvStatus.text = "Preparing game…"
            binding.progressLaunch.visibility = android.view.View.VISIBLE

            val result = gameUtils.launchGame(ip, port, playerName, password)
            when (result) {
                is GameUtils.LaunchResult.Success -> {
                    sessionStart = System.currentTimeMillis()
                    binding.tvStatus.text = "Game launched ✓"
                    startOverlayIfEnabled()
                    startVoiceChatIfEnabled(ip, port)
                    viewModel.recordConnection(ip, port)
                    // Keep activity running to track session
                }
                is GameUtils.LaunchResult.Error -> {
                    binding.tvStatus.text = "Error: ${result.message}"
                    toast(result.message)
                }
            }
        }
    }

    private fun startOverlayIfEnabled() {
        if (permissionUtils.hasOverlayPermission()) {
            startService(Intent(this, InGameOverlayService::class.java))
        }
    }

    private fun startVoiceChatIfEnabled(ip: String, port: Int) {
        if (viewModel.isVoiceChatEnabled() && permissionUtils.hasMicrophonePermission()) {
            val intent = Intent(this, VoiceChatService::class.java).apply {
                putExtra(Constants.EXTRA_SERVER_IP,   ip)
                putExtra(Constants.EXTRA_SERVER_PORT, port)
            }
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop overlay + voice chat when returning
        stopService(Intent(this, InGameOverlayService::class.java))
        stopService(Intent(this, VoiceChatService::class.java))
        // Record play time
        if (sessionStart > 0) {
            val playTime = System.currentTimeMillis() - sessionStart
            viewModel.recordSession(playTime)
        }
    }
}
