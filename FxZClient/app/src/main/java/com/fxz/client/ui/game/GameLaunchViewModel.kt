package com.fxz.client.ui.game

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.repository.ProfileRepository
import com.fxz.client.repository.ServerRepository
import com.fxz.client.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameLaunchViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val profileRepository: ProfileRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    fun recordConnection(ip: String, port: Int) {
        viewModelScope.launch {
            serverRepository.updateLastConnected(ip, port)
            prefs.edit().putString(Constants.PREF_LAST_SERVER, "$ip:$port").apply()
        }
    }

    fun recordSession(playTimeMs: Long) {
        viewModelScope.launch {
            val activeProfile = profileRepository.getActiveProfile().first()
            if (activeProfile != null) {
                profileRepository.addSession(activeProfile.id, playTimeMs)
            }
        }
    }

    fun isVoiceChatEnabled(): Boolean =
        prefs.getBoolean(Constants.PREF_VOICE_CHAT, false)
}
