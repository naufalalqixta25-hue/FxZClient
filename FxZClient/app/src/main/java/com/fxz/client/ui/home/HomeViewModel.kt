package com.fxz.client.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.data.model.DeviceStats
import com.fxz.client.data.model.NewsItem
import com.fxz.client.data.model.PlayerProfile
import com.fxz.client.data.model.Server
import com.fxz.client.data.remote.ApiService
import com.fxz.client.repository.GameRepository
import com.fxz.client.repository.ProfileRepository
import com.fxz.client.repository.ServerRepository
import com.fxz.client.utils.DeviceInfo
import com.fxz.client.utils.RamCleaner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val serverRepository: ServerRepository,
    private val gameRepository: GameRepository,
    private val deviceInfo: DeviceInfo,
    private val ramCleaner: RamCleaner,
    private val apiService: ApiService
) : ViewModel() {

    val activeProfile: StateFlow<PlayerProfile?> =
        profileRepository.getActiveProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentServers: StateFlow<List<Server>> =
        serverRepository.getRecentServers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteServers: StateFlow<List<Server>> =
        serverRepository.getFavoriteServers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGameReady = MutableStateFlow(false)
    val isGameReady: StateFlow<Boolean> = _isGameReady

    private val _deviceStats = MutableStateFlow<DeviceStats?>(null)
    val deviceStats: StateFlow<DeviceStats?> = _deviceStats

    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news

    private val _lastServer = MutableStateFlow<Server?>(null)
    val lastServer: StateFlow<Server?> = _lastServer

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage

    init {
        checkGameReady()
        refreshDeviceStats()
        loadNews()
    }

    private fun checkGameReady() {
        viewModelScope.launch {
            _isGameReady.value = gameRepository.isGameSetupComplete()
        }
    }

    fun refreshDeviceStats() {
        viewModelScope.launch {
            _deviceStats.value = deviceInfo.getDeviceStats()
        }
    }

    private fun loadNews() {
        viewModelScope.launch {
            runCatching {
                val response = apiService.getNews()
                if (response.isSuccessful) _news.value = response.body() ?: emptyList()
            }
        }
    }

    fun quickConnect(server: Server) {
        viewModelScope.launch {
            serverRepository.updateLastConnected(server.ip, server.port)
            _lastServer.value = server
        }
    }

    fun refreshLastServer(ip: String, port: Int) {
        viewModelScope.launch {
            val info = serverRepository.queryServer(ip, port)
            if (info != null) {
                _lastServer.value = Server(
                    ip = ip, port = port, name = info.name,
                    players = info.players, maxPlayers = info.maxPlayers,
                    ping = info.ping, mode = info.mode
                )
            }
        }
    }

    fun getRamPercent(): Int = ramCleaner.getRamUsagePercent()
}
