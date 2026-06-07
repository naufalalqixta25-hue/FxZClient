package com.fxz.client.ui.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.data.model.PlayerProfile
import com.fxz.client.repository.ProfileRepository
import com.fxz.client.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    val profiles: StateFlow<List<PlayerProfile>> =
        repo.getAllProfiles().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<PlayerProfile?> =
        repo.getActiveProfile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage

    private val _showCreateDialog = MutableSharedFlow<Unit>()
    val showCreateDialog: SharedFlow<Unit> = _showCreateDialog

    fun createProfile(name: String, password: String) {
        if (name.isBlank()) { viewModelScope.launch { _uiMessage.emit("Name cannot be empty") }; return }
        if (name.length < 3) { viewModelScope.launch { _uiMessage.emit("Name must be 3+ chars") }; return }
        viewModelScope.launch {
            val profile = repo.createProfile(name.trim(), password)
            prefs.edit().putString(Constants.PREF_PLAYER_NAME, profile.name).apply()
            if (password.isNotBlank())
                prefs.edit().putString(Constants.PREF_PLAYER_PASS, password).apply()
            _uiMessage.emit("Profile created: ${profile.name}")
        }
    }

    fun setActive(profile: PlayerProfile) {
        viewModelScope.launch {
            repo.setActiveProfile(profile.id)
            prefs.edit().putString(Constants.PREF_PLAYER_NAME, profile.name).apply()
            _uiMessage.emit("Switched to ${profile.name}")
        }
    }

    fun deleteProfile(profile: PlayerProfile) {
        viewModelScope.launch {
            repo.deleteProfile(profile)
            _uiMessage.emit("Profile deleted")
        }
    }

    fun getSavedName(): String = prefs.getString(Constants.PREF_PLAYER_NAME, "") ?: ""
    fun getSavedPassword(): String = prefs.getString(Constants.PREF_PLAYER_PASS, "") ?: ""

    fun saveQuick(name: String, pass: String) {
        prefs.edit()
            .putString(Constants.PREF_PLAYER_NAME, name)
            .putString(Constants.PREF_PLAYER_PASS, pass)
            .apply()
        viewModelScope.launch { _uiMessage.emit("Saved: $name") }
    }

    fun requestShowCreate() {
        viewModelScope.launch { _showCreateDialog.emit(Unit) }
    }
}
