package com.fxz.client.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.fxz.client.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPreferences
) {
    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    fun getCurrentTheme(): String =
        prefs.getString(Constants.PREF_THEME, Constants.THEME_NEON_BLUE) ?: Constants.THEME_NEON_BLUE

    fun setTheme(theme: String) {
        prefs.edit().putString(Constants.PREF_THEME, theme).apply()
    }

    fun getAccentColor(): Int = when (getCurrentTheme()) {
        Constants.THEME_NEON_GREEN  -> 0xFF00FF88.toInt()
        Constants.THEME_NEON_RED    -> 0xFFFF3355.toInt()
        Constants.THEME_NEON_PURPLE -> 0xFFAA55FF.toInt()
        else                        -> 0xFF00B4FF.toInt()  // Default neon blue
    }

    fun getThemeList() = listOf(
        ThemeOption(Constants.THEME_NEON_BLUE,   "Neon Blue",   0xFF00B4FF.toInt()),
        ThemeOption(Constants.THEME_NEON_GREEN,  "Neon Green",  0xFF00FF88.toInt()),
        ThemeOption(Constants.THEME_NEON_RED,    "Neon Red",    0xFFFF3355.toInt()),
        ThemeOption(Constants.THEME_NEON_PURPLE, "Neon Purple", 0xFFAA55FF.toInt())
    )
    data class ThemeOption(val id: String, val name: String, val color: Int)
}
