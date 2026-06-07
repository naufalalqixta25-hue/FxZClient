plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.fxz.client"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fxz.client"
        minSdk = 26
        targetSdk = 34
        versionCode = 100
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        buildConfigField("String", "BASE_CDN_URL",    "\"https://cdn.fxzclient.com/\"")
        buildConfigField("String", "API_BASE_URL",    "\"https://api.fxzclient.com/v1/\"")
        buildConfigField("String", "UPDATE_URL",      "\"https://update.fxzclient.com/\"")
        buildConfigField("String", "SAMP_QUERY_HOST", "\"query.sa-mp.com\"")
        buildConfigField("int",    "SAMP_QUERY_PORT", "7777")
    }

    signingConfigs {
        create("release") {
            // Set your keystore details here or use environment variables
            // storeFile = file("keystore.jks")
            // storePassword = "your_password"
            // keyAlias = "fxzclient"
            // keyPassword = "your_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
    }
}

dependencies {
    // ── Core ─────────────────────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")

    // ── UI ───────────────────────────────────────────────────────────────
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // ── Navigation ────────────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ── DI – Hilt ─────────────────────────────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // ── Database – Room ───────────────────────────────────────────────────
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Network ───────────────────────────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ── Coroutines ────────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ── WorkManager ───────────────────────────────────────────────────────
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ── Image loading ─────────────────────────────────────────────────────
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    // ── Preferences ───────────────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // ── Security ──────────────────────────────────────────────────────────
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ── Animation ─────────────────────────────────────────────────────────
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // ── Compression ───────────────────────────────────────────────────────
    implementation("org.apache.commons:commons-compress:1.24.0")

    // ── Testing ───────────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt { correctErrorTypes = true }
