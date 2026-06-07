package com.fxz.client.utils

object Constants {

    // ── App ─────────────────────────────────────────────────────────────
    const val APP_NAME          = "FxZ Client"
    const val APP_VERSION       = "1.0.0"
    const val APP_VERSION_CODE  = 100

    // ── Notification Channels ────────────────────────────────────────────
    const val CHANNEL_DOWNLOAD  = "fxz_download"
    const val CHANNEL_GAME      = "fxz_game"
    const val CHANNEL_UPDATE    = "fxz_update"
    const val CHANNEL_GENERAL   = "fxz_general"

    // ── Notification IDs ─────────────────────────────────────────────────
    const val NOTIF_DOWNLOAD    = 1001
    const val NOTIF_GAME        = 1002
    const val NOTIF_UPDATE      = 1003
    const val NOTIF_OVERLAY     = 1004

    // ── Storage Paths ────────────────────────────────────────────────────
    const val FXZ_BASE_DIR      = "FxZClient"
    const val GAME_DATA_DIR     = "game_data"
    const val SAMP_DIR          = "samp"
    const val CONFIG_DIR        = "config"
    const val SCREENSHOTS_DIR   = "screenshots"
    const val CACHE_DIR         = "cache"
    const val BACKUP_DIR        = "backup"

    // ── GTA SA Android Package ───────────────────────────────────────────
    const val GTASA_PACKAGE     = "com.rockstar.gtasa"
    const val GTASA_OBB_DIR     = "Android/obb/com.rockstar.gtasa"
    const val GTASA_MAIN_OBB    = "main.2360.com.rockstar.gtasa.obb"
    const val GTASA_PATCH_OBB   = "patch.2360.com.rockstar.gtasa.obb"

    // ── SA:MP Files ──────────────────────────────────────────────────────
    const val SAMP_LIB          = "libsamp.so"
    const val SAMP_CONFIG       = "settings.json"
    const val SAMP_SCRIPTS_DIR  = "cleo"

    // ── CDN URLs ─────────────────────────────────────────────────────────
    const val CDN_BASE          = "https://cdn.fxzclient.com/"
    const val CDN_GTASA_DATA    = "${CDN_BASE}gtasa/data.zip"
    const val CDN_SAMP_LIB      = "${CDN_BASE}samp/libsamp.so"
    const val CDN_SAMP_SCRIPTS  = "${CDN_BASE}samp/cleo.zip"
    const val CDN_UPDATE_JSON   = "${CDN_BASE}update/latest.json"

    // ── File Sizes (bytes) ────────────────────────────────────────────────
    const val GTASA_DATA_SIZE   = 2_400_000_000L  // ~2.4 GB
    const val SAMP_LIB_SIZE     = 8_500_000L      // ~8.5 MB

    // ── SA:MP Query Protocol ─────────────────────────────────────────────
    const val SAMP_QUERY_MAGIC  = "SAMP"
    const val SAMP_QUERY_INFO   = 'i'
    const val SAMP_QUERY_RULES  = 'r'
    const val SAMP_QUERY_CLIENT = 'c'
    const val SAMP_QUERY_DETAIL = 'd'
    const val SAMP_QUERY_PING   = 'p'
    const val SAMP_TIMEOUT_MS   = 3000
    const val SAMP_DEFAULT_PORT = 7777

    // ── Preferences Keys ─────────────────────────────────────────────────
    const val PREF_PLAYER_NAME      = "pref_player_name"
    const val PREF_PLAYER_PASS      = "pref_player_password"
    const val PREF_THEME            = "pref_theme"
    const val PREF_FPS_LOCK         = "pref_fps_lock"
    const val PREF_SENSITIVITY      = "pref_sensitivity"
    const val PREF_PERF_MODE        = "pref_performance_mode"
    const val PREF_BATTERY_SAVER    = "pref_battery_saver"
    const val PREF_ANTI_LAG         = "pref_anti_lag"
    const val PREF_GPU_OPT          = "pref_gpu_optimization"
    const val PREF_CROSSHAIR        = "pref_crosshair"
    const val PREF_CUSTOM_HUD       = "pref_custom_hud"
    const val PREF_VOICE_CHAT       = "pref_voice_chat"
    const val PREF_AUDIO_ENHANCER   = "pref_audio_enhancer"
    const val PREF_MUSIC_MENU       = "pref_music_menu"
    const val PREF_CLOUD_BACKUP     = "pref_cloud_backup"
    const val PREF_STORAGE_PATH     = "pref_storage_path"
    const val PREF_LAST_SERVER      = "pref_last_server"
    const val PREF_SHOW_OVERLAY     = "pref_show_overlay"
    const val PREF_FIRST_LAUNCH     = "pref_first_launch"
    const val PREF_SETUP_DONE       = "pref_setup_done"

    // ── FPS Options ───────────────────────────────────────────────────────
    val FPS_OPTIONS = listOf(30, 45, 60, 90, 120)

    // ── Theme Options ─────────────────────────────────────────────────────
    const val THEME_NEON_BLUE   = "neon_blue"
    const val THEME_NEON_GREEN  = "neon_green"
    const val THEME_NEON_RED    = "neon_red"
    const val THEME_NEON_PURPLE = "neon_purple"

    // ── Intent Actions ────────────────────────────────────────────────────
    const val ACTION_CONNECT_SERVER = "com.fxz.client.CONNECT_SERVER"
    const val ACTION_STOP_DOWNLOAD  = "com.fxz.client.STOP_DOWNLOAD"
    const val ACTION_OVERLAY_HIDE   = "com.fxz.client.OVERLAY_HIDE"

    // ── Intent Extras ─────────────────────────────────────────────────────
    const val EXTRA_SERVER_IP   = "extra_server_ip"
    const val EXTRA_SERVER_PORT = "extra_server_port"
    const val EXTRA_SERVER_NAME = "extra_server_name"
    const val EXTRA_PASSWORD    = "extra_password"

    // ── Database ─────────────────────────────────────────────────────────
    const val DB_NAME           = "fxz_client.db"
    const val DB_VERSION        = 1

    // ── WorkManager Tags ─────────────────────────────────────────────────
    const val WORK_DOWNLOAD     = "work_download"
    const val WORK_BACKUP       = "work_backup"
    const val WORK_UPDATE_CHECK = "work_update_check"
}
