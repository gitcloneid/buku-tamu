package com.hv.bukutm.presentation.screen.dashboard.penerimatamu

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQrViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _appointment = mutableStateOf<Appointment?>(null)
    val appointment: State<Appointment?> = _appointment

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Add the missing updateStatusSuccess property
    private val _updateStatusSuccess = mutableStateOf<Boolean?>(null)
    val updateStatusSuccess: State<Boolean?> = _updateStatusSuccess

    fun fetchAppointmentByQr(kodeQr: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _appointment.value = null
            try {
                val token = tokenManager.accessToken.firstOrNull()
                if (token == null) {
                    _errorMessage.value = "Token otentikasi tidak ditemukan. Silakan login kembali."
                    Log.e("ScanQrViewModel", "No token available")
                    return@launch
                }
                Log.d("ScanQrViewModel", "Fetching appointment for QR: $kodeQr with token: $token")
                val result = repository.getAppointmentByQr(token, kodeQr)
                result.fold(
                    onSuccess = { appointment ->
                        _appointment.value = appointment
                        Log.d("ScanQrViewModel", "Appointment fetched: ${appointment.idJanjiTemu}")
                    },
                    onFailure = { error ->
                        _errorMessage.value = when {
                            error.message?.contains("HTTP 400") == true -> "Janji temu sudah dalam status Selesai atau Janji temu tidak untuk hari ini."
                            error.message?.contains("HTTP 401") == true -> "Sesi telah berakhir. Silakan login kembali."
                            error.message?.contains("HTTP 404") == true -> "Error: Kode QR tidak ditemukan."
                            error.message?.contains("Network error") == true -> "Kesalahan jaringan. Periksa koneksi Anda."
                            else -> error.message ?: "Gagal mengambil data janji temu."
                        }
                        Log.e("ScanQrViewModel", "Error fetching appointment: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan tak terduga: ${e.message}"
                Log.e("ScanQrViewModel", "Unexpected error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointmentStatus(id: Int, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _updateStatusSuccess.value = null
            try {
                val token = tokenManager.accessToken.firstOrNull()
                if (token == null) {
                    _errorMessage.value = "Token otentikasi tidak ditemukan. Silakan login kembali."
                    Log.e("ScanQrViewModel", "No token available")
                    return@launch
                }
                Log.d("ScanQrViewModel", "Updating appointment $id to status: $status with token: $token")
                val result = repository.updateAppointmentStatus(token, id, status)
                result.fold(
                    onSuccess = { appointment ->
                        _appointment.value = appointment
                        _updateStatusSuccess.value = true
                        Log.d("ScanQrViewModel", "Appointment status updated: ${appointment.idJanjiTemu}, Status: ${appointment.status}")
                    },
                    onFailure = { error ->
                        _errorMessage.value = when {
                            error.message?.contains("HTTP 400") == true -> "Janji temu sudah dalam status Selesai atau status tidak valid."
                            error.message?.contains("HTTP 401") == true -> "Sesi telah berakhir. Silakan login kembali."
                            error.message?.contains("HTTP 404") == true -> "Error: Janji temu tidak ditemukan."
                            error.message?.contains("Network error") == true -> "Kesalahan jaringan. Periksa koneksi Anda."
                            else -> error.message ?: "Gagal memperbarui status janji temu."
                        }
                        _updateStatusSuccess.value = false
                        Log.e("ScanQrViewModel", "Error updating status: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan tak terduga: ${e.message}"
                _updateStatusSuccess.value = false
                Log.e("ScanQrViewModel", "Unexpected error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rescheduleAppointment(id: Int, tanggal: String, waktu: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val token = tokenManager.accessToken.firstOrNull()
                if (token == null) {
                    _errorMessage.value = "Token otentikasi tidak ditemukan. Silakan login kembali."
                    Log.e("ScanQrViewModel", "No token available")
                    onComplete(false)
                    return@launch
                }
                Log.d("ScanQrViewModel", "Rescheduling appointment $id to $tanggal $waktu with token: $token")
                val result = repository.rescheduleAppointment(token, id, tanggal, waktu)
                result.fold(
                    onSuccess = { appointment ->
                        _appointment.value = appointment
                        Log.d("ScanQrViewModel", "Appointment rescheduled: ${appointment.idJanjiTemu}, Date: ${appointment.tanggal}, Time: ${appointment.waktu}")
                        onComplete(true)
                    },
                    onFailure = { error ->
                        _errorMessage.value = when {
                            error.message?.contains("HTTP 400") == true -> "Gagal menjadwalkan ulang: Data tidak valid atau janji temu tidak dapat dijadwalkan ulang."
                            error.message?.contains("HTTP 401") == true -> "Sesi telah berakhir. Silakan login kembali."
                            error.message?.contains("HTTP 404") == true -> "Error: Janji temu tidak ditemukan."
                            error.message?.contains("Network error") == true -> "Kesalahan jaringan. Periksa koneksi Anda."
                            else -> error.message ?: "Gagal menjadwalkan ulang janji temu."
                        }
                        Log.e("ScanQrViewModel", "Error rescheduling appointment: ${error.message}")
                        onComplete(false)
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan tak terduga: ${e.message}"
                Log.e("ScanQrViewModel", "Unexpected error: ${e.message}", e)
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Add the missing clearUpdateStatus function
    fun clearUpdateStatus() {
        _updateStatusSuccess.value = null
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }
}