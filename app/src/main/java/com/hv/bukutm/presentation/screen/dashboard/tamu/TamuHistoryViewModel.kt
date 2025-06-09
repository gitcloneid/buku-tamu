package com.hv.bukutm.presentation.screen.dashboard.tamu

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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TamuHistoryUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedLastMonths: Int? = 6 // Default to 6 months
)

@HiltViewModel
class TamuHistoryViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TamuHistoryUiState())
    val uiState: StateFlow<TamuHistoryUiState> = _uiState.asStateFlow()

    init {
        fetchTamuHistory(6) // Default fetch with 6 months
    }

    fun setLastMonthsFilter(lastMonths: Int?) {
        _uiState.update { it.copy(selectedLastMonths = lastMonths) }
        fetchTamuHistory(lastMonths)
    }

    private fun fetchTamuHistory(lastMonths: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val token = tokenManager.accessToken.firstOrNull() ?: "" // Use empty string if token is null
            val kodeQr = tokenManager.tamu.firstOrNull()?.kodeQr

            if (kodeQr == null) {
                _uiState.update { it.copy(isLoading = false, error = "Kode QR tidak ditemukan") }
                return@launch
            }

            // Fetch appointment by QR code to get the phone number
            val qrResult = appointmentRepository.getTamuByQr(token = token, kodeQr = kodeQr)

            qrResult.fold(
                onSuccess = { appointment ->
                    val phoneNumber = appointment.tamu.telepon
                    if (phoneNumber == null) {
                        _uiState.update { it.copy(isLoading = false, error = "Nomor telepon tamu tidak ditemukan") }
                        return@fold
                    }

                    // Fetch appointment history with the phone number and lastMonths filter
                    val result = appointmentRepository.getTamuHistory(
                        token = token,
                        lastmonth = lastMonths,
                        phoneNumber = phoneNumber
                    )

                    result.fold(
                        onSuccess = { appointments ->
                            // Sort appointments in descending order by date and time
                            val sortedAppointments = appointments.sortedWith(
                                compareByDescending<Appointment> {
                                    LocalDate.parse(it.tanggal, DateTimeFormatter.ISO_LOCAL_DATE)
                                }.thenByDescending {
                                    it.waktu?.let { time -> LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")) }
                                        ?: LocalTime.MIN
                                }
                            )
                            _uiState.update { it.copy(appointments = sortedAppointments, isLoading = false, error = null) }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isLoading = false, error = "Gagal memuat riwayat: ${error.message}") }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = "Gagal memuat data janji temu: ${error.message}") }
                }
            )
        }
    }
}