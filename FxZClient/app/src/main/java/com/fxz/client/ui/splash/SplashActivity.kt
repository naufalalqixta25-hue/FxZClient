package com.fxz.client.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fxz.client.databinding.ActivitySplashBinding
import com.fxz.client.ui.main.MainActivity
import com.fxz.client.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        lifecycleScope.launch {
            // ── Phase 1: Logo fade-in ────────────────────────────────────
            binding.logoContainer.alpha = 0f
            binding.logoContainer.scaleX = 0.6f
            binding.logoContainer.scaleY = 0.6f
            delay(200)

            binding.logoContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
            delay(700)

            // ── Phase 2: Tagline slide-up ────────────────────────────────
            binding.tvTagline.translationY = 40f
            binding.tvTagline.alpha = 0f
            binding.tvTagline.visibility = View.VISIBLE
            binding.tvTagline.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .start()
            delay(500)

            // ── Phase 3: Progress bar fill ────────────────────────────────
            binding.progressBar.visibility = View.VISIBLE
            animateProgressBar()
            delay(1200)

            // ── Phase 4: Version / build info ────────────────────────────
            binding.tvVersion.alpha = 0f
            binding.tvVersion.visibility = View.VISIBLE
            binding.tvVersion.text = "v${Constants.APP_VERSION}"
            binding.tvVersion.animate().alpha(0.6f).setDuration(300).start()
            delay(500)

            // ── Navigate ────────────────────────────────────────────────
            navigateToMain()
        }
    }

    private fun animateProgressBar() {
        val progressAnim = android.animation.ObjectAnimator.ofInt(
            binding.progressBar, "progress", 0, 100
        )
        progressAnim.duration = 1000
        progressAnim.interpolator = AccelerateDecelerateInterpolator()
        progressAnim.start()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
