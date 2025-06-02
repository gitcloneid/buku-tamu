package com.hv.bukutm.domain.repository

import com.hv.bukutm.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(token: String, limit: Int): Result<List<Notification>>
}