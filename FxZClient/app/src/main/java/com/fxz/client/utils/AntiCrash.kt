package com.fxz.client.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object AntiCrash {

    private const val TAG = "FxZ-AntiCrash"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(context, throwable)
            Log.e(TAG, "Uncaught exception on thread: ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashLog(context: Context, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val crashDir = File(context.filesDir, "crashes")
            crashDir.mkdirs()
            val crashFile = File(crashDir, "crash_${System.currentTimeMillis()}.txt")
            crashFile.writeText("""
                FxZ Client Crash Report
                =======================
                Time: ${java.util.Date()}
                Thread: ${Thread.currentThread().name}
                
                $sw
            """.trimIndent())
        } catch (_: Exception) {}
    }
}
