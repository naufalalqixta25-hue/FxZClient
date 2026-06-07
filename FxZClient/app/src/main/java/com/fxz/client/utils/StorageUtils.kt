package com.fxz.client.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Returns path with most free space from external storages */
    fun getOptimalStoragePath(): String {
        val paths = getAvailableStoragePaths()
        return paths.maxByOrNull { getFreeSpace(it) }
            ?: Environment.getExternalStorageDirectory().absolutePath
    }

    fun getPrimaryExternalStorage(): String =
        Environment.getExternalStorageDirectory().absolutePath

    fun getAvailableStoragePaths(): List<String> {
        val paths = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getExternalFilesDirs(null).filterNotNull().forEach {
                // Walk up to the root of external storage
                var f: File? = it
                while (f != null && !Environment.isExternalStorageEmulated(f)) f = f.parentFile
                (f?.parentFile ?: it.parentFile?.parentFile?.parentFile?.parentFile)
                    ?.absolutePath?.let { p -> paths.add(p) }
            }
        }
        if (paths.isEmpty()) paths.add(Environment.getExternalStorageDirectory().absolutePath)
        return paths.distinct()
    }

    fun getFreeSpace(path: String): Long {
        return try {
            StatFs(path).let { it.availableBlocksLong * it.blockSizeLong }
        } catch (e: Exception) { 0L }
    }

    fun getTotalSpace(path: String): Long {
        return try {
            StatFs(path).let { it.totalBlocksLong * it.blockSizeLong }
        } catch (e: Exception) { 0L }
    }

    fun hasEnoughSpace(path: String, requiredBytes: Long): Boolean =
        getFreeSpace(path) >= requiredBytes + 500_000_000L // +500 MB buffer

    fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024L         -> "%.0f KB".format(bytes / 1_024.0)
        else                    -> "$bytes B"
    }

    fun formatSpeed(bytesPerSec: Long): String = when {
        bytesPerSec >= 1_048_576L -> "%.1f MB/s".format(bytesPerSec / 1_048_576.0)
        bytesPerSec >= 1_024L     -> "%.0f KB/s".format(bytesPerSec / 1_024.0)
        else                      -> "$bytesPerSec B/s"
    }
}
