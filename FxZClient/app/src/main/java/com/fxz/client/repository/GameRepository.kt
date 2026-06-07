package com.fxz.client.repository

import android.content.Context
import com.fxz.client.utils.Constants
import com.fxz.client.utils.StorageUtils
import com.fxz.client.utils.FileValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageUtils: StorageUtils,
    private val fileValidator: FileValidator
) {
    fun getGameBasePath(): String = storageUtils.getOptimalStoragePath()

    fun getGameDataPath(): String =
        "${getGameBasePath()}/${Constants.FXZ_BASE_DIR}/${Constants.GAME_DATA_DIR}"

    fun getSampPath(): String =
        "${getGameBasePath()}/${Constants.FXZ_BASE_DIR}/${Constants.SAMP_DIR}"

    fun getObbPath(): String {
        val extStorage = storageUtils.getPrimaryExternalStorage()
        return "${extStorage}/${Constants.GTASA_OBB_DIR}"
    }

    suspend fun isGameSetupComplete(): Boolean = withContext(Dispatchers.IO) {
        val obbPath = getObbPath()
        val sampPath = getSampPath()
        val mainObb  = File("$obbPath/${Constants.GTASA_MAIN_OBB}")
        val sampLib  = File("$sampPath/${Constants.SAMP_LIB}")
        mainObb.exists() && mainObb.length() > 1_000_000 &&
            sampLib.exists() && sampLib.length() > 100_000
    }

    suspend fun validateGameFiles(): ValidationResult = withContext(Dispatchers.IO) {
        val obbPath  = getObbPath()
        val sampPath = getSampPath()
        val errors   = mutableListOf<String>()

        val mainObb  = File("$obbPath/${Constants.GTASA_MAIN_OBB}")
        if (!mainObb.exists())       errors.add("GTA SA main OBB missing")
        else if (mainObb.length() < 1_000_000) errors.add("GTA SA main OBB corrupted")

        val sampLib  = File("$sampPath/${Constants.SAMP_LIB}")
        if (!sampLib.exists())       errors.add("SA:MP library missing")
        else if (sampLib.length() < 100_000) errors.add("SA:MP library corrupted")

        if (errors.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(errors)
    }

    fun ensureDirectories() {
        listOf(getGameDataPath(), getSampPath(), getObbPath(),
            "${getGameBasePath()}/${Constants.FXZ_BASE_DIR}/${Constants.CONFIG_DIR}",
            "${getGameBasePath()}/${Constants.FXZ_BASE_DIR}/${Constants.SCREENSHOTS_DIR}"
        ).forEach { File(it).mkdirs() }
    }

    suspend fun cleanCache() = withContext(Dispatchers.IO) {
        File("${getGameBasePath()}/${Constants.FXZ_BASE_DIR}/${Constants.CACHE_DIR}").deleteRecursively()
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }
}
