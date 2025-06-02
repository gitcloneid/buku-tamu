package com.hv.bukutm.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface WhatsAppApi {
    @POST("api/wa/send-media")
    suspend fun sendMedia(
        @Body request: WhatsAppMediaRequest
    ): WhatsAppMediaResponse
}

data class WhatsAppMediaRequest(
    val to: String,
    val mediaBase64: String,
    val filename: String,
    val caption: String
)

data class WhatsAppMediaResponse(
    val status: String
)