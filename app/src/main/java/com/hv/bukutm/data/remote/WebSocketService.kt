package com.hv.bukutm.data.remote.websocket

import kotlinx.coroutines.flow.Flow

interface WebSocketService {
    fun connect(url: String, authToken: String): Flow<String?>
    fun disconnect()
}