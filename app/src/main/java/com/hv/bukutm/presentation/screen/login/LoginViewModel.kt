package com.hv.bukutm.presentation.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Email atau Password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            authRepository.login(email, password).fold(
                onSuccess = { user ->
                    // Ensure tokens are saved correctly
                    val accessToken = user.token
                    val refreshToken = user.refreshToken ?: ""
                    if (accessToken.isNotBlank()) {
                        tokenManager.saveTokens(accessToken, refreshToken)
                        Log.d("LoginViewModel", "Tokens Saved - Access: $accessToken, Refresh: $refreshToken")
                        _uiState.value = LoginUiState(user = user)
                    } else {
                        _uiState.value = LoginUiState(error = "Access token is empty")
                    }
                },
                onFailure = { error ->
                    val errorMessage = when {
                        error.message?.matches(Regex("HTTP (40[1-9]|4[1-9][0-9])")) == true -> {
                            "Email atau Password Salah"
                        }
                        error.message?.contains("HTTP 400") == true -> {
                            "Email atau Password Salah"
                        }
                        else -> {
                            "Email atau Password Salah"
                        }
                    }
                    _uiState.value = LoginUiState(error = errorMessage)
                }
            )
        }
    }
}