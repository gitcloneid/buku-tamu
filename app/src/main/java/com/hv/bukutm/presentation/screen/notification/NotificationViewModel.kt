    package com.hv.bukutm.presentation.screen.notification

    import android.util.Log
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.google.gson.Gson
    import com.hv.bukutm.HiltInit
    import com.hv.bukutm.data.TokenManager
    import com.hv.bukutm.data.remote.WebSocketNotification
    import com.hv.bukutm.data.remote.websocket.WebSocketService
    import com.hv.bukutm.domain.model.Notification
    import com.hv.bukutm.domain.repository.NotificationRepository
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.collectLatest
    import kotlinx.coroutines.flow.firstOrNull
    import kotlinx.coroutines.launch
    import javax.inject.Inject
    import android.app.Application

    @HiltViewModel
    class NotificationViewModel @Inject constructor(
        private val notificationRepository: NotificationRepository,
        private val tokenManager: TokenManager,
        private val webSocketService: WebSocketService,
        private val gson: Gson
    ) : ViewModel() {


        private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
        val notifications: StateFlow<List<Notification>> = _notifications

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

        private val _realtimeNotification = MutableStateFlow<WebSocketNotification?>(null)
        val realtimeNotification: StateFlow<WebSocketNotification?> = _realtimeNotification

        init {
            loadNotifications()
            connectWebSocket()
        }

        private fun loadNotifications() {
            viewModelScope.launch {
                _isLoading.value = true
                tokenManager.accessToken.firstOrNull()?.let { token ->
                    val result = notificationRepository.getNotifications(token, 20)
                    result.onSuccess { fetchedNotifications ->
                        _notifications.value = fetchedNotifications
                        _isLoading.value = false
                    }
                    result.onFailure { error ->
                        _errorMessage.value = error.localizedMessage
                        _isLoading.value = false
                        Log.e("NotificationViewModel ", "Error fetching notifications", error)
                    }
                } ?: run {
                    _errorMessage.value = "No token available"
                    _isLoading.value = false
                }
            }
        }

        private fun connectWebSocket() {
            viewModelScope.launch {
                tokenManager.accessToken.firstOrNull()?.let {  token ->
                    try {
                        webSocketService.connect("ws://api-sub.clowlaw.my.id/api/ws", token)
                            .collectLatest { message ->
                                message?.let {
                                    try {
                                        val webSocketNotification = gson.fromJson(it, WebSocketNotification::class.java)
                                        Log.d("WebSocket", "Received WebSocket message: $webSocketNotification")
                                        val newNotification = Notification(
                                            idNotifikasi = webSocketNotification.userId,
                                            pesan = webSocketNotification.message,
                                            waktu = webSocketNotification.timestamp.substringBefore("."),
                                            isRead = false
                                        )
                                        _notifications.value = listOf(newNotification) + _notifications.value
                                        _realtimeNotification.value = webSocketNotification
                                    } catch (e: Exception) {
                                        Log.e("WebSocket", "Error parsing WebSocket message: $it", e)
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        Log.e("WebSocket", "WebSocket connection failed", e)
                        _errorMessage.value = "Failed to connect to WebSocket"
                    }
                } ?: Log.d("WebSocket", "No token available, cannot connect WebSocket")
            }
        }

        override fun onCleared() {
            super.onCleared()
            webSocketService.disconnect()
        }
    }
