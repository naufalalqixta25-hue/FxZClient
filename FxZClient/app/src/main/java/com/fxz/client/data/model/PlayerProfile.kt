package com.fxz.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class PlayerProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val password: String = "",
    val avatarUrl: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val totalPlayTime: Long = 0,
    val serversJoined: Int = 0,
    val lastSeen: Long = 0
)
