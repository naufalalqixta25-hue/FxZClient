package com.fxz.client.utils

import android.content.Context
import com.google.gson.Gson
import com.fxz.client.data.model.GameConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageUtils: StorageUtils,
    private val gson: Gson
) {
    private val backupDir get() = File(
        "${storageUtils.getGameBasePath()}/${Constants.FXZ_BASE_DIR}/${Constants.BACKUP_DIR}"
    ).also { it.mkdirs() }

    private fun StorageUtils.getGameBasePath() = getOptimalStoragePath()

    suspend fun backupConfig(config: GameConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(config)
            File(backupDir, "config_backup.json").writeText(json)
            true
        } catch (_: Exception) { false }
    }

    suspend fun restoreConfig(): GameConfig? = withContext(Dispatchers.IO) {
        try {
            val json = File(backupDir, "config_backup.json").readText()
            gson.fromJson(json, GameConfig::class.java)
        } catch (_: Exception) { null }
    }

    suspend fun exportProfile(name: String, data: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(backupDir, "profile_${name}.json").writeText(data)
            true
        } catch (_: Exception) { false }
    }

    fun getBackupFiles(): List<File> = backupDir.listFiles()?.toList() ?: emptyList()

    suspend fun deleteBackup(file: File): Boolean = withContext(Dispatchers.IO) {
        runCatching { file.delete() }.getOrDefault(false)
    }
}
