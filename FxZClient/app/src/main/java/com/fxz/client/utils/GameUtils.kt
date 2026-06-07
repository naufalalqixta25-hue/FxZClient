package com.fxz.client.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.fxz.client.repository.GameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameRepository: GameRepository,
    private val ramCleaner: RamCleaner,
    private val performanceManager: PerformanceManager,
    private val prefs: android.content.SharedPreferences
) {
    companion object {
        private const val TAG = "FxZ-GameUtils"
        private const val GTASA_PKG = "com.rockstar.gtasa"
        private const val SAMP_ACTIVITY = "com.rockstar.gtasa.GTASA"
    }

    /**
     * Main game launch entry point.
     * Performs pre-launch optimizations, then fires the SA:MP intent.
     */
    suspend fun launchGame(ip: String, port: Int, playerName: String, password: String = ""): LaunchResult =
        withContext(Dispatchers.IO) {
            try {
                // ── 1. Validate game files ────────────────────────────────
                val validation = gameRepository.validateGameFiles()
                if (validation is GameRepository.ValidationResult.Invalid) {
                    return@withContext LaunchResult.Error("Game files invalid: ${validation.errors.joinToString()}")
                }

                // ── 2. Inject SA:MP config ────────────────────────────────
                injectSampConfig(ip, port, playerName, password)

                // ── 3. Pre-launch optimizations ───────────────────────────
                ramCleaner.cleanBeforeLaunch()
                val perfMode = prefs.getBoolean(Constants.PREF_PERF_MODE, false)
                if (perfMode) performanceManager.applyPerformanceMode(true)

                // ── 4. Apply FPS lock ─────────────────────────────────────
                val fps = prefs.getInt(Constants.PREF_FPS_LOCK, 60)
                injectFpsConfig(fps)

                // ── 5. Launch GTA SA with SA:MP mod ───────────────────────
                val launchIntent = buildLaunchIntent(ip, port)
                context.startActivity(launchIntent)

                Log.i(TAG, "Game launched: $ip:$port as $playerName")
                LaunchResult.Success

            } catch (e: Exception) {
                Log.e(TAG, "Launch failed", e)
                LaunchResult.Error(e.message ?: "Unknown error")
            }
        }

    private fun buildLaunchIntent(ip: String, port: Int): Intent {
        // Try to launch via the installed GTA SA package with SA:MP intent
        val pm = context.packageManager
        return try {
            pm.getLaunchIntentForPackage(GTASA_PKG)!!.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("samp_ip",   ip)
                putExtra("samp_port", port)
            }
        } catch (e: Exception) {
            // Fallback: direct component launch
            Intent().apply {
                setPackage(GTASA_PKG)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("samp_ip",   ip)
                putExtra("samp_port", port)
            }
        }
    }

    private fun injectSampConfig(ip: String, port: Int, name: String, password: String) {
        val sampPath = gameRepository.getSampPath()
        val configFile = File("$sampPath/${Constants.SAMP_CONFIG}")
        configFile.parentFile?.mkdirs()
        val config = buildString {
            appendLine("{")
            appendLine("  \"ip\": \"$ip\",")
            appendLine("  \"port\": $port,")
            appendLine("  \"name\": \"$name\",")
            if (password.isNotBlank()) appendLine("  \"password\": \"$password\",")
            appendLine("  \"version\": \"0.3.7-R4\"")
            append("}")
        }
        configFile.writeText(config)
    }

    private fun injectFpsConfig(fps: Int) {
        val sampPath = gameRepository.getSampPath()
        val fpsFile = File("$sampPath/fps.cfg")
        fpsFile.writeText(fps.toString())
    }

    suspend fun extractZip(
        zipFile: File,
        destDir: File,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            destDir.mkdirs()
            val totalSize = zipFile.length()
            var processed = 0L
            ZipArchiveInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextZipEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(destDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { fos ->
                            val buf = ByteArray(65536)
                            var n: Int
                            while (zis.read(buf).also { n = it } != -1) {
                                fos.write(buf, 0, n)
                                processed += n
                                onProgress(((processed.toDouble() / totalSize) * 100).toInt().coerceIn(0, 100))
                            }
                        }
                    }
                    entry = zis.nextZipEntry
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Zip extraction failed", e)
            false
        }
    }

    fun isGtaSaInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(GTASA_PKG, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) { false }

    sealed class LaunchResult {
        object Success : LaunchResult()
        data class Error(val message: String) : LaunchResult()
    }
}
