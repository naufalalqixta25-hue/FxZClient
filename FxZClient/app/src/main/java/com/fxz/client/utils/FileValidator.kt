package com.fxz.client.utils

import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileValidator @Inject constructor() {

    fun validateMd5(file: File, expectedMd5: String): Boolean {
        if (!file.exists() || expectedMd5.isBlank()) return file.exists()
        return try {
            val md5 = computeMd5(file)
            md5.equals(expectedMd5, ignoreCase = true)
        } catch (_: Exception) { false }
    }

    fun computeMd5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun isValidObb(file: File): Boolean {
        if (!file.exists()) return false
        if (file.length() < 1_000_000) return false
        return file.name.endsWith(".obb")
    }

    fun isValidSampLib(file: File): Boolean {
        if (!file.exists()) return false
        if (file.length() < 100_000) return false
        // Check ELF header
        return try {
            val header = ByteArray(4)
            file.inputStream().use { it.read(header) }
            header[0] == 0x7F.toByte() &&
                header[1] == 'E'.code.toByte() &&
                header[2] == 'L'.code.toByte() &&
                header[3] == 'F'.code.toByte()
        } catch (_: Exception) { false }
    }
}
