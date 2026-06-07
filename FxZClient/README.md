# FxZ Client — SA:MP Android Launcher

> Ultimate SA:MP (San Andreas Multiplayer) client for Android

## Features
- Auto-download GTA SA data & SA:MP libraries
- One-click connect to any SA:MP server
- Real-time FPS/RAM overlay
- Server browser with live ping
- Custom themes (Neon Blue / Green / Red / Purple)
- In-game overlay, voice chat, audio enhancer
- OTA auto-update system
- Cloud config backup
- Anti-crash, anti-lag, GPU/RAM optimization

## Build Requirements
- Android Studio Hedgehog or newer
- Kotlin 1.9.22
- Android SDK 34
- Gradle 8.6

## Setup
1. Clone the repo
2. Copy `local.properties.template` → `local.properties`
3. Set your `sdk.dir` path
4. Run `./gradlew assembleDebug`
5. APK will be in `app/build/outputs/apk/debug/`

## Architecture
- **MVVM** + Hilt DI
- **Room** for local DB (servers, profiles, downloads)
- **Retrofit** + OkHttp for API & CDN downloads
- **Navigation Component** with bottom nav
- **Coroutines + Flow** for reactive state
- **WorkManager** for background tasks

## SA:MP Integration
The launcher automatically:
1. Downloads GTA SA data to optimal storage path
2. Places SA:MP `.so` libraries in the correct directory
3. Injects server IP/port into SA:MP config
4. Launches the patched game via Android intent

## Minimum: Android 8.0 (API 26) | Target: Android 14 (API 34)
## ABIs: arm64-v8a, armeabi-v7a
