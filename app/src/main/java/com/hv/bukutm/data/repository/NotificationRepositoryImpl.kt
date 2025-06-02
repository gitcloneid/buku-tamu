package com.hv.bukutm.data.repository

import com.hv.bukutm.data.remote.NotificationApi
import com.hv.bukutm.domain.model.Notification
import com.hv.bukutm.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {
    override suspend fun getNotifications(token: String, limit: Int): Result<List<Notification>> {
        return try {
            val response = notificationApi.getNotifications("Bearer $token", limit)
            val notifications = response.data.map {
                Notification(
                    idNotifikasi = it.idNotifikasi,
                    pesan = it.pesan,
                    waktu = it.waktu,
                    isRead = it.isRead
                )
            }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}