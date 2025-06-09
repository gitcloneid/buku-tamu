package com.hv.bukutm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.repository.AuthRepository
import com.hv.bukutm.utils.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Expose tokenManager so it can be passed to the NavGraph from MainActivity
    val publicTokenManager: TokenManager = tokenManager

    init {
        viewModelScope.launch {
            val refreshToken = tokenManager.refreshToken.firstOrNull()
            if (refreshToken != null) {
                // A refresh token exists, so we attempt to refresh the session.
                Log.d("MainViewModel", "Refresh token found. Attempting to refresh.")
                performTokenRefresh(refreshToken)
            } else {
                // If there's no refresh token, check for a guest (Tamu) session.
                val tamu = tokenManager.tamu.firstOrNull()
                if (tamu != null) {
                    Log.d("MainViewModel", "Tamu session found.")
                    _authState.value = AuthState.Authenticated("Tamu")
                } else {
                    // No session found, user is unauthenticated.
                    Log.d("MainViewModel", "No tokens or Tamu session found. User is unauthenticated.")
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Performs the token refresh and updates the authState based on the result.
     */
    private fun performTokenRefresh(refreshToken: String) {
        viewModelScope.launch {
            authRepository.refreshToken(refreshToken).fold(
                onSuccess = { (accessToken, newRefreshToken) ->
                    Log.d("MainViewModel", "Token Refreshed Successfully.")
                    tokenManager.saveTokens(accessToken, newRefreshToken)

                    val role = JwtUtils.getRoleFromToken(accessToken)

                    // **FIX**: Check if the role is valid (not null) before setting the state.
                    // This prevents the "Type Mismatch" error.
                    if (role != null) {
                        _authState.value = AuthState.Authenticated(role)
                    } else {
                        // If the token is valid but doesn't contain a role,
                        // treat the user as unauthenticated.
                        Log.e("MainViewModel", "Role not found in token. Clearing session.")
                        tokenManager.clearTokens()
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { error ->
                    // If the refresh call fails, the token is likely expired or invalid.
                    Log.e("MainViewModel", "Refresh Failed: ${error.message}")
                    tokenManager.clearTokens()
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }
}
