package com.fxz.client.repository

import com.fxz.client.BuildConfig
import com.fxz.client.data.model.AppUpdateInfo
import com.fxz.client.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun checkForUpdate(): Boolean {
        return try {
            val response = api.checkUpdate()
            if (response.isSuccessful) {
                val info = response.body() ?: return false
                info.versionCode > BuildConfig.VERSION_CODE
            } else false
        } catch (e: Exception) { false }
    }

    suspend fun getUpdateInfo(): AppUpdateInfo? {
        return try {
            api.checkUpdate().body()
        } catch (e: Exception) { null }
    }
}
