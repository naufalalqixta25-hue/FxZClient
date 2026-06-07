package com.fxz.client.ui.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxz.client.data.model.Server
import com.fxz.client.data.model.SampServerInfo
import com.fxz.client.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ServerBrowserViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _allServers = MutableStateFlow<List<Server>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortMode   = MutableStateFlow(SortMode.PLAYERS)
    private val _isRefreshing = MutableStateFlow(false)
    private val _uiMessage  = MutableSharedFlow<String>()
    private val _pingResult = MutableSharedFlow<Pair<String, Int>>()
    private val _queryResult = MutableStateFlow<SampServerInfo?>(null)

    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    val uiMessage: SharedFlow<String>   = _uiMessage
    val queryResult: StateFlow<SampServerInfo?> = _queryResult

    val filteredServers: StateFlow<List<Server>> = combine(
        _allServers, _searchQuery, _sortMode
    ) { servers, query, sort ->
        var list = if (query.isBlank()) servers
        else servers.filter { it.displayName.contains(query, ignoreCase = true) || it.address.contains(query) }
        when (sort) {
            SortMode.PLAYERS  -> list.sortedByDescending { it.players }
            SortMode.PING     -> list.sortedWith(compareBy { if (it.ping < 0) Int.MAX_VALUE else it.ping })
            SortMode.NAME     -> list.sortedBy { it.displayName.lowercase() }
            SortMode.FAVORITE -> list.sortedWith(compareByDescending<Server> { it.isFavorite }.thenByDescending { it.lastConnected })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteServers: StateFlow<List<Server>> =
        serverRepository.getFavoriteServers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            serverRepository.getAllServers().collect { _allServers.value = it }
        }
    }

    fun setSearch(q: String)    { _searchQuery.value = q }
    fun setSortMode(m: SortMode){ _sortMode.value = m }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            serverRepository.refreshAll()
            _isRefreshing.value = false
        }
    }

    fun toggleFavorite(server: Server) {
        viewModelScope.launch {
            serverRepository.toggleFavorite(server)
            val msg = if (!server.isFavorite) "Added to favorites" else "Removed from favorites"
            _uiMessage.emit(msg)
        }
    }

    fun addServer(ip: String, port: Int) {
        viewModelScope.launch {
            val server = serverRepository.addServer(ip, port)
            pingServer(server)
        }
    }

    fun pingServer(server: Server) {
        viewModelScope.launch {
            val ping = serverRepository.pingServer(server.ip, server.port)
            _pingResult.emit(server.address to ping)
            serverRepository.refreshServerInfo(server)
        }
    }

    fun queryAddress(ip: String, port: Int) {
        viewModelScope.launch {
            _queryResult.value = serverRepository.queryServer(ip, port)
        }
    }

    fun deleteServer(server: Server) {
        viewModelScope.launch {
            serverRepository.deleteServer(server)
            _uiMessage.emit("Server removed")
        }
    }

    enum class SortMode { PLAYERS, PING, NAME, FAVORITE }
}
