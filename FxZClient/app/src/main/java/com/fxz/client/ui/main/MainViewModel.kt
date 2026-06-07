package com.fxz.client.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.repository.GameRepository
import com.fxz.client.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete: StateFlow<Boolean> = _isSetupComplete

    private val _hasUpdate = MutableStateFlow(false)
    val hasUpdate: StateFlow<Boolean> = _hasUpdate

    init {
        checkSetupStatus()
        checkForUpdates()
    }

    private fun checkSetupStatus() {
        viewModelScope.launch {
            _isSetupComplete.value = gameRepository.isGameSetupComplete()
        }
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            runCatching { _hasUpdate.value = updateRepository.checkForUpdate() }
        }
    }
}
