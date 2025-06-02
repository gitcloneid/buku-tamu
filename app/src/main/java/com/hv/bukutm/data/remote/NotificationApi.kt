package com.hv.bukutm.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") authToken: String,
        @Query("limit") limit: Int = 10
    ): NotificationListResponse
}

data class NotificationListResponse(
    val data: List<NotificationResponse>
)

data class NotificationResponse(
    val idNotifikasi: Int,
    val pesan: String,
    val waktu: String,
    val isRead: Boolean
)

data class WebSocketNotification(
    val userId: Int,
    val message: String,
    val timestamp: String
)