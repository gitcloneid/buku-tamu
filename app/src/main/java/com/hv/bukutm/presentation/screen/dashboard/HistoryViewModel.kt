package com.hv.bukutm.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        fetchCompletedAppointments()
    }

    private fun fetchCompletedAppointments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val token = tokenManager.accessToken.firstOrNull()
            if (token != null) {
                val result = appointmentRepository.getCompletedAppointments(token, page = 1, limit = 10)
                result.fold(
                    onSuccess = { appointments ->
                        _uiState.update { it.copy(appointments = appointments, isLoading = false, error = null) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Token tidak ditemukan") }
            }
        }
    }
}