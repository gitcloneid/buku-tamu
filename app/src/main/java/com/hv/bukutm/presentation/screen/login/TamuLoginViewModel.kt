package com.hv.bukutm.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.Tamu
import com.hv.bukutm.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TamuLoginUiState(
    val tamu: Tamu? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TamuLoginViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TamuLoginUiState())
    val uiState: StateFlow<TamuLoginUiState> = _uiState.asStateFlow()

    fun loginWithQrCode(qrCode: String) {
        if (qrCode.isBlank()) {
            _uiState.update { it.copy(error = "Kode QR tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = appointmentRepository.getTamuByQr(token = null, kodeQr = qrCode)
                result.fold(
                    onSuccess = { appointment ->
                        val tamu = appointment.tamu.copy(kodeQr = appointment.kodeQr)
                        tokenManager.saveTamu(tamu, appointment.kodeQr)
                        _uiState.update {
                            it.copy(
                                tamu = tamu,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Gagal masuk: Kode QR tidak ditemukan"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Terjadi kesalahan: Server Unreachable"
                    )
                }
            }
        }
    }
}