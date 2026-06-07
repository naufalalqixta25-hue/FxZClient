package com.fxz.client.data.local.dao

import androidx.room.*
import com.fxz.client.data.model.Server
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY isFavorite DESC, lastConnected DESC")
    fun getAllServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE isFavorite = 1 ORDER BY lastConnected DESC")
    fun getFavoriteServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE lastConnected > 0 ORDER BY lastConnected DESC LIMIT 10")
    fun getRecentServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE ip = :ip AND port = :port LIMIT 1")
    suspend fun getServer(ip: String, port: Int): Server?

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: Int): Server?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server): Long

    @Update
    suspend fun updateServer(server: Server)

    @Delete
    suspend fun deleteServer(server: Server)

    @Query("UPDATE servers SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Int, fav: Boolean)

    @Query("UPDATE servers SET lastConnected = :time WHERE ip = :ip AND port = :port")
    suspend fun updateLastConnected(ip: String, port: Int, time: Long)

    @Query("UPDATE servers SET players = :players, ping = :ping, name = :name WHERE ip = :ip AND port = :port")
    suspend fun updateServerInfo(ip: String, port: Int, players: Int, ping: Int, name: String)

    @Query("DELETE FROM servers WHERE isFavorite = 0 AND lastConnected = 0")
    suspend fun cleanUnusedServers()
}
