package com.fxz.client.repository

import com.fxz.client.data.local.dao.ServerDao
import com.fxz.client.data.model.SampServerInfo
import com.fxz.client.data.model.Server
import com.fxz.client.data.remote.SampQueryProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val serverDao: ServerDao
) {
    fun getAllServers(): Flow<List<Server>> = serverDao.getAllServers()
    fun getFavoriteServers(): Flow<List<Server>> = serverDao.getFavoriteServers()
    fun getRecentServers(): Flow<List<Server>> = serverDao.getRecentServers()

    suspend fun addServer(ip: String, port: Int): Server {
        val existing = serverDao.getServer(ip, port)
        if (existing != null) return existing
        val server = Server(ip = ip, port = port)
        val id = serverDao.insertServer(server)
        return server.copy(id = id.toInt())
    }

    suspend fun saveServer(server: Server) {
        serverDao.insertServer(server)
    }

    suspend fun deleteServer(server: Server) = serverDao.deleteServer(server)

    suspend fun toggleFavorite(server: Server) {
        serverDao.setFavorite(server.id, !server.isFavorite)
    }

    suspend fun refreshServerInfo(server: Server): Server {
        val info: SampServerInfo? = SampQueryProtocol.queryServer(server.ip, server.port)
        return if (info != null) {
            val updated = server.copy(
                name       = info.name,
                mode       = info.mode,
                map        = info.map,
                language   = info.language,
                players    = info.players,
                maxPlayers = info.maxPlayers,
                hasPassword = info.hasPassword,
                ping       = info.ping
            )
            serverDao.updateServer(updated)
            updated
        } else {
            server.copy(ping = -1).also { serverDao.updateServer(it) }
        }
    }

    suspend fun refreshAll() {
        val servers = serverDao.getAllServers().first()
        servers.forEach { refreshServerInfo(it) }
    }

    suspend fun updateLastConnected(ip: String, port: Int) {
        serverDao.updateLastConnected(ip, port, System.currentTimeMillis())
    }

    suspend fun queryServer(ip: String, port: Int): SampServerInfo? =
        SampQueryProtocol.queryServer(ip, port)

    suspend fun pingServer(ip: String, port: Int): Int =
        SampQueryProtocol.pingServer(ip, port)
}
