package com.fxz.client.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageUtils: StorageUtils
) {
    suspend fun saveScreenshot(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName  = "FxZ_${timestamp}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/FxZClient")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return@withContext Result.failure(Exception("Failed to create media entry"))
                context.contentResolver.openOutputStream(uri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
                Result.success("Pictures/FxZClient/$fileName")
            } else {
                val dir = File(
                    "${storageUtils.getPrimaryExternalStorage()}/Pictures/FxZClient"
                ).apply { mkdirs() }
                val file = File(dir, fileName)
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
                Result.success(file.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getScreenshotsDir(): File {
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(null), "screenshots")
        } else {
            File("${storageUtils.getPrimaryExternalStorage()}/Pictures/FxZClient")
        }
        return dir.also { it.mkdirs() }
    }
}
