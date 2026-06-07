package com.fxz.client.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.fxz.client.BuildConfig
import com.fxz.client.data.local.AppDatabase
import com.fxz.client.data.local.dao.DownloadDao
import com.fxz.client.data.local.dao.PlayerProfileDao
import com.fxz.client.data.local.dao.ServerDao
import com.fxz.client.data.remote.ApiService
import com.fxz.client.utils.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides @Singleton
    fun provideSharedPreferences(@ApplicationContext ctx: Context): SharedPreferences =
        ctx.getSharedPreferences("fxz_prefs", Context.MODE_PRIVATE)

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, Constants.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideServerDao(db: AppDatabase): ServerDao = db.serverDao()

    @Provides @Singleton
    fun provideProfileDao(db: AppDatabase): PlayerProfileDao = db.profileDao()

    @Provides @Singleton
    fun provideDownloadDao(db: AppDatabase): DownloadDao = db.downloadDao()
}
