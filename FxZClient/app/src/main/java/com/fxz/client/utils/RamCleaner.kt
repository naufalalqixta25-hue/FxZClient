package com.fxz.client.utils

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RamCleaner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun cleanBeforeLaunch() {
        System.gc()
        System.runFinalization()
        Runtime.getRuntime().let {
            it.gc()
            it.runFinalization()
        }
    }

    fun getAvailableRam(): Long {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.availMem
    }

    fun getTotalRam(): Long {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.totalMem
    }

    fun getRamUsagePercent(): Int {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return ((1.0 - info.availMem.toDouble() / info.totalMem) * 100).toInt()
    }

    fun isLowMemory(): Boolean {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.lowMemory
    }
}
