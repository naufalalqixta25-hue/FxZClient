package com.fxz.client.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DISCORD_INVITE  = "https://discord.gg/fxzclient"
        const val TELEGRAM_GROUP  = "https://t.me/fxzclient"
        const val GITHUB_REPO     = "https://github.com/fxzclient/android"
        const val YOUTUBE_CHANNEL = "https://youtube.com/@fxzclient"
    }

    fun openDiscord()   = openUrl(DISCORD_INVITE)
    fun openTelegram()  = openUrl(TELEGRAM_GROUP)
    fun openGithub()    = openUrl(GITHUB_REPO)
    fun openYoutube()   = openUrl(YOUTUBE_CHANNEL)

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "FxZ Client — SA:MP Android")
            putExtra(Intent.EXTRA_TEXT,
                "Play SA:MP on Android with FxZ Client!\n$DISCORD_INVITE")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Share FxZ Client").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
}
