package com.hv.bukutm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.repository.AuthRepository
import com.hv.bukutm.utils.JwtUtils
import com.hv.bukutm.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Defines the possible states of user authentication, now including the user's role.
 */
sealed interface AuthState {
    object Loading : AuthState
    data class Authenticated(val role: String) : AuthState // Role must be a non-null String
    object Unauthenticated : AuthState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _showNoInternetDialog = MutableStateFlow(false)
    val showNoInternetDialog: StateFlow<Boolean> = _showNoInternetDialog.asStateFlow()

    val publicTokenManager: TokenManager = tokenManager

    init {
        viewModelScope.launch {
            val refreshToken = tokenManager.refreshToken.firstOrNull()
            if (refreshToken != null) {
                if (NetworkUtils.isInternetAvailable(context)) {
                    Log.d("MainViewModel", "Refresh token found. Attempting to refresh.")
                    performTokenRefresh(refreshToken)
                } else {
                    Log.d("MainViewModel", "No internet connection. Showing dialog.")
                    _showNoInternetDialog.value = true
                    // Don't change authState, keep it as Loading until user retries
                }
            } else {
                val tamu = tokenManager.tamu.firstOrNull()
                if (tamu != null) {
                    Log.d("MainViewModel", "Tamu session found.")
                    _authState.value = AuthState.Authenticated("Tamu")
                } else {
                    Log.d("MainViewModel", "No tokens or Tamu session found. User is unauthenticated.")
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun retryAuthCheck() {
        viewModelScope.launch {
            _showNoInternetDialog.value = false
            _authState.value = AuthState.Loading

            val refreshToken = tokenManager.refreshToken.firstOrNull()
            if (refreshToken != null) {
                if (NetworkUtils.isInternetAvailable(context)) {
                    performTokenRefresh(refreshToken)
                } else {
                    _showNoInternetDialog.value = true
                }
            } else {
                val tamu = tokenManager.tamu.firstOrNull()
                if (tamu != null) {
                    _authState.value = AuthState.Authenticated("Tamu")
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    private fun performTokenRefresh(refreshToken: String) {
        viewModelScope.launch {
            authRepository.refreshToken(refreshToken).fold(
                onSuccess = { (accessToken, newRefreshToken) ->
                    Log.d("MainViewModel", "Token Refreshed Successfully.")
                    tokenManager.saveTokens(accessToken, newRefreshToken)

                    val role = JwtUtils.getRoleFromToken(accessToken)
                    if (role != null) {
                        _authState.value = AuthState.Authenticated(role)
                    } else {
                        Log.e("MainViewModel", "Role not found in token. Clearing session.")
                        tokenManager.clearTokens()
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { error ->
                    Log.e("MainViewModel", "Refresh Failed: ${error.message}")
                    tokenManager.clearTokens()
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }
}
