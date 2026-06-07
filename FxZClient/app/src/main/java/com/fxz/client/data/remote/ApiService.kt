package com.fxz.client.data.remote

import com.fxz.client.data.model.AppUpdateInfo
import com.fxz.client.data.model.NewsItem
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("update/check")
    suspend fun checkUpdate(): Response<AppUpdateInfo>

    @GET("news")
    suspend fun getNews(): Response<List<NewsItem>>

    @GET("servers/featured")
    suspend fun getFeaturedServers(): Response<List<RemoteServer>>

    @GET("servers/list")
    suspend fun getServerList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("sort") sort: String = "players"
    ): Response<ServerListResponse>

    @POST("analytics/session")
    suspend fun reportSession(@Body body: SessionReport): Response<Unit>
}

data class RemoteServer(
    val ip: String,
    val port: Int,
    val name: String,
    val mode: String,
    val players: Int,
    val maxPlayers: Int,
    val isOfficial: Boolean
)

data class ServerListResponse(
    val servers: List<RemoteServer>,
    val total: Int,
    val page: Int
)

data class SessionReport(
    val deviceModel: String,
    val androidVersion: String,
    val clientVersion: String,
    val playTime: Long
)
