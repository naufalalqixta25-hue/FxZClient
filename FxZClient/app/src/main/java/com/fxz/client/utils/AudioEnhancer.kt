package com.fxz.client.utils

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEnhancer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun enable(audioSessionId: Int) {
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                setParameter(Equalizer.PARAM_CURRENT_PRESET, 3) // Rock preset
                enabled = true
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                setStrength(400)
                enabled = true
            }
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                setTargetGain(500)
                enabled = true
            }
        } catch (_: Exception) {}
    }

    fun disable() {
        runCatching { equalizer?.enabled = false; equalizer?.release(); equalizer = null }
        runCatching { bassBoost?.enabled = false; bassBoost?.release(); bassBoost = null }
        runCatching { loudnessEnhancer?.enabled = false; loudnessEnhancer?.release(); loudnessEnhancer = null }
    }

    fun setVolume(level: Float) {
        am.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * level).toInt(),
            0
        )
    }

    fun getVolumePercent(): Int {
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return if (max > 0) (cur * 100 / max) else 0
    }
}
