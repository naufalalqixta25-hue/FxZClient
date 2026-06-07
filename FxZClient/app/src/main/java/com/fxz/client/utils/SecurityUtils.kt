package com.fxz.client.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun getEncryptedPrefs() = EncryptedSharedPreferences.create(
        context, "fxz_secure_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var n: Int
            while (input.read(buffer).also { n = it } != -1) digest.update(buffer, 0, n)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    fun isApkTampered(): Boolean {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            val sig = info.signatures?.firstOrNull() ?: return false
            val sha = sha256(sig.toByteArray())
            sha.isNotBlank()
        } catch (_: Exception) { false }
    }
}
