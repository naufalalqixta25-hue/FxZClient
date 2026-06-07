package com.fxz.client.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.fxz.client.data.model.AppUpdateInfo
import com.fxz.client.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtaUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateRepository: UpdateRepository,
    private val okHttpClient: OkHttpClient
) {
    suspend fun checkAndPromptUpdate(): AppUpdateInfo? =
        updateRepository.getUpdateInfo()

    suspend fun downloadAndInstall(
        info: AppUpdateInfo,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val apkFile = File(context.cacheDir, "fxz_update_${info.versionCode}.apk")
            val request = Request.Builder().url(info.downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body ?: return@withContext false
            val total = body.contentLength()
            var downloaded = 0L

            apkFile.outputStream().use { out ->
                body.byteStream().use { input ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) {
                        out.write(buf, 0, n)
                        downloaded += n
                        if (total > 0) onProgress(((downloaded * 100) / total).toInt())
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.provider", apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(installIntent)
            true
        } catch (_: Exception) { false }
    }
}
