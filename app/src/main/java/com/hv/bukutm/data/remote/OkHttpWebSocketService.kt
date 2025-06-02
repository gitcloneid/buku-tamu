package com.hv.bukutm.data.remote.websocket

import android.util.Log
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient
) : WebSocketService, WebSocketListener() {

    private var webSocket: WebSocket? = null
    private lateinit var channel: ProducerScope<String?>

    override fun connect(url: String, authToken: String): Flow<String?> = callbackFlow {
        this@OkHttpWebSocketService.channel = this

        webSocket = okHttpClient.newWebSocket(
            Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $authToken")
                .build(),
            this@OkHttpWebSocketService
        )

        awaitClose {
            Log.d("WebSocketService", "WebSocket Disconnected")
            webSocket?.cancel()
        }
    }

    override fun disconnect() {
        webSocket?.cancel()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocketService", "WebSocket Opened: ${response.message}")
        this.webSocket = webSocket
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocketService", "Received message: $text")
        channel.trySend(text).isSuccess
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocketService", "WebSocket Closing: $code $reason")
        channel.close()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocketService", "WebSocket Closed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocketService", "WebSocket Failure: ${t.message}", t)
        channel.close(t)
    }
}
