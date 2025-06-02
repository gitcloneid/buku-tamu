package com.hv.bukutm.domain.model

data class Notification(
    val idNotifikasi: Int,
    val pesan: String,
    val waktu: String,
    val isRead: Boolean
)