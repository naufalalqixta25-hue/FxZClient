# FxZ Client ProGuard Rules

# Keep Kotlin metadata
-keepattributes *Annotation*, Signature, Exception
-keepattributes SourceFile, LineNumberTable
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# Retrofit + OkHttp
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }
-keep class com.fxz.client.data.model.** { *; }
-keep class com.fxz.client.data.remote.** { *; }
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }

# Navigation
-keep class androidx.navigation.** { *; }

# Data models (Parcelable)
-keep class com.fxz.client.data.model.Server { *; }
-keep class com.fxz.client.data.model.PlayerProfile { *; }
-keep class com.fxz.client.data.model.DownloadTask { *; }
-keep class com.fxz.client.data.model.GameConfig { *; }
-keep class com.fxz.client.data.model.SampServerInfo { *; }

# Crash handler
-keep class com.fxz.client.utils.AntiCrash { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# DataStore
-keep class androidx.datastore.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
