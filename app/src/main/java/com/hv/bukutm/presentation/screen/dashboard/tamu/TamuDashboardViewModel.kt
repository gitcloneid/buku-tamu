package com.hv.bukutm.presentation.screen.home

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

data class TamuDashboardUiState(
    val appointment: Appointment? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TamuDashboardViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TamuDashboardUiState())
    val uiState: StateFlow<TamuDashboardUiState> = _uiState.asStateFlow()

    init {
        fetchAppointment()
    }

    private fun fetchAppointment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tamu = tokenManager.tamu.firstOrNull()
                if (tamu == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Data tamu tidak ditemukan"
                        )
                    }
                    return@launch
                }

                val kodeQr = tamu.kodeQr ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Kode QR tidak tersedia"
                        )
                    }
                    return@launch
                }

                val result = appointmentRepository.getTamuByQr(token = null, kodeQr = kodeQr)
                result.fold(
                    onSuccess = { appointment ->
                        _uiState.update {
                            it.copy(
                                appointment = appointment,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Gagal memuat data janji temu: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }
}