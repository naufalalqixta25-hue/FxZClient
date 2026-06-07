package com.fxz.client.data.local.dao

import androidx.room.*
import com.fxz.client.data.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM profiles ORDER BY isActive DESC, lastSeen DESC")
    fun getAllProfiles(): Flow<List<PlayerProfile>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<PlayerProfile?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfile): Long

    @Update
    suspend fun updateProfile(profile: PlayerProfile)

    @Delete
    suspend fun deleteProfile(profile: PlayerProfile)

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Int)

    @Query("UPDATE profiles SET totalPlayTime = totalPlayTime + :time, serversJoined = serversJoined + 1, lastSeen = :now WHERE id = :id")
    suspend fun addSession(id: Int, time: Long, now: Long)
}
