package com.fxz.client.repository

import com.fxz.client.data.local.dao.PlayerProfileDao
import com.fxz.client.data.model.PlayerProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: PlayerProfileDao
) {
    fun getAllProfiles(): Flow<List<PlayerProfile>> = profileDao.getAllProfiles()
    fun getActiveProfile(): Flow<PlayerProfile?> = profileDao.getActiveProfile()

    suspend fun createProfile(name: String, password: String): PlayerProfile {
        val profile = PlayerProfile(name = name, password = password)
        val id = profileDao.insertProfile(profile)
        profileDao.deactivateAll()
        profileDao.setActive(id.toInt())
        return profile.copy(id = id.toInt(), isActive = true)
    }

    suspend fun setActiveProfile(id: Int) {
        profileDao.deactivateAll()
        profileDao.setActive(id)
    }

    suspend fun deleteProfile(profile: PlayerProfile) = profileDao.deleteProfile(profile)

    suspend fun addSession(profileId: Int, playTime: Long) {
        profileDao.addSession(profileId, playTime, System.currentTimeMillis())
    }

    suspend fun updateProfile(profile: PlayerProfile) = profileDao.updateProfile(profile)
}
