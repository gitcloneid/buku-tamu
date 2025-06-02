package com.hv.bukutm.presentation.screen.dashboard.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.data.api.MonthlyReport
import com.hv.bukutm.domain.repository.ReportsRepository
import com.hv.bukutm.utils.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MonthlyReportViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = mutableStateOf(MonthlyReportUiState())
    val uiState: State<MonthlyReportUiState> = _uiState

    init {
        fetchUserInfo()
        fetchMonthlyReport()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            tokenManager.accessToken.firstOrNull()?.let { token ->
                val userName = JwtUtils.getNameFromToken(token)
                val userRole = JwtUtils.getRoleFromToken(token)

                _uiState.value = _uiState.value.copy(
                    userName = userName ?: "",
                    userRole = userRole ?: ""
                )
            }
        }
    }

    fun fetchMonthlyReport(month: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            tokenManager.accessToken.firstOrNull()?.let { token ->
                reportsRepository.getMonthlyReport(token, month).fold(
                    onSuccess = { report ->
                        _uiState.value = _uiState.value.copy(
                            monthlyReport = report,
                            isLoading = false,
                            selectedMonth = month ?: report.month
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Unknown error occurred",
                            isLoading = false
                        )
                    }
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "Authentication token not found",
                    isLoading = false
                )
            }
        }
    }

    fun getMonthName(month: Int): String {
        return try {
            Month.of(month).getDisplayName(TextStyle.FULL, Locale("id", "ID"))
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class MonthlyReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val monthlyReport: MonthlyReport? = null,
    val userName: String = "",
    val userRole: String = "",
    val selectedMonth: Int = java.time.LocalDate.now().monthValue
)