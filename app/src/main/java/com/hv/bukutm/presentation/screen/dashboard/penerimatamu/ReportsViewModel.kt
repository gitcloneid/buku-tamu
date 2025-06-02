package com.hv.bukutm.presentation.screen.dashboard.penerimatamu

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.data.api.DailyReport
import com.hv.bukutm.domain.repository.ReportsRepository
import com.hv.bukutm.utils.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ReportsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = mutableStateOf(ReportsUiState())
    val uiState: State<ReportsUiState> = _uiState

    init {
        fetchDailyReport()
        fetchUserData()
    }

    private fun fetchUserData() {
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

    fun fetchDailyReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.accessToken.firstOrNull()
                if (token == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Token otentikasi tidak ditemukan. Silakan login kembali."
                    )
                    Log.e("ReportsViewModel", "No token available")
                    return@launch
                }
                Log.d("ReportsViewModel", "Fetching daily report with token: $token")
                val result = repository.getDailyReport(token)
                result.fold(
                    onSuccess = { report ->
                        _uiState.value = _uiState.value.copy(
                            dailyReport = report,
                            isLoading = false
                        )
                        Log.d("ReportsViewModel", "Daily report fetched: ${report.date}")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = when {
                                error.message?.contains("HTTP 401") == true -> "Error: Tidak diizinkan. Silakan login kembali."
                                error.message?.contains("HTTP 404") == true -> "Error: Laporan harian tidak ditemukan."
                                error.message?.contains("Network error") == true -> "Kesalahan jaringan. Periksa koneksi Anda."
                                else -> error.message ?: "Gagal mengambil laporan harian."
                            }
                        )
                        Log.e("ReportsViewModel", "Error fetching daily report: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan tak terduga: ${e.message}"
                )
                Log.e("ReportsViewModel", "Unexpected error: ${e.message}", e)
            }
        }
    }
}

data class ReportsUiState(
    val dailyReport: DailyReport? = null,
    val userName: String = "Admin",
    val userRole: String = "Administrator",
    val isLoading: Boolean = false,
    val error: String? = null
)