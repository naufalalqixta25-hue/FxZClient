package com.fxz.client.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "servers")
data class Server(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ip: String,
    val port: Int = 7777,
    val name: String = "",
    val mode: String = "",
    val map: String = "",
    val language: String = "",
    val players: Int = 0,
    val maxPlayers: Int = 500,
    val ping: Int = -1,
    val hasPassword: Boolean = false,
    val isFavorite: Boolean = false,
    val isOfficial: Boolean = false,
    val lastConnected: Long = 0,
    val customName: String = "",
    val customPassword: String = ""
) : Parcelable {

    val address: String get() = "$ip:$port"
    val displayName: String get() = customName.ifBlank { name.ifBlank { address } }
    val isOnline: Boolean get() = ping >= 0
    val pingQuality: PingQuality get() = when {
        ping < 0   -> PingQuality.UNKNOWN
        ping < 80  -> PingQuality.EXCELLENT
        ping < 150 -> PingQuality.GOOD
        ping < 300 -> PingQuality.FAIR
        else       -> PingQuality.POOR
    }
    val playerRatio: Float get() = if (maxPlayers > 0) players.toFloat() / maxPlayers else 0f

    enum class PingQuality { UNKNOWN, EXCELLENT, GOOD, FAIR, POOR }
}

@Parcelize
data class SampServerInfo(
    val ip: String,
    val port: Int,
    val name: String,
    val mode: String,
    val map: String,
    val language: String,
    val players: Int,
    val maxPlayers: Int,
    val hasPassword: Boolean,
    val ping: Int,
    val rules: Map<String, String> = emptyMap()
) : Parcelable
