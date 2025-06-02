package com.hv.bukutm.presentation.screen.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.domain.repository.AppointmentRepository
import com.hv.bukutm.utils.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardGuruUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "",
    val userRole: String = ""
)

@HiltViewModel
class DashboardGuruViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(DashboardGuruUiState())
    val uiState: DashboardGuruUiState get() = _uiState.value

    init {
        loadUserData()
        fetchTodayAppointments()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            tokenManager.accessToken.collectLatest { token ->
                if (token != null) {
                    val name = JwtUtils.getNameFromToken(token) ?: "Unknown"
                    val role = JwtUtils.getRoleFromToken(token) ?: "Unknown"
                    _uiState.value = _uiState.value.copy(userName = name, userRole = role)
                }
            }
        }
    }

    internal fun fetchTodayAppointments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            tokenManager.accessToken.collectLatest { token ->
                if (token != null) {
                    appointmentRepository.getTodayAppointments(token).fold(
                        onSuccess = { appointments ->
                            _uiState.value = _uiState.value.copy(
                                appointments = appointments,
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    )
                }
            }
        }
    }
}