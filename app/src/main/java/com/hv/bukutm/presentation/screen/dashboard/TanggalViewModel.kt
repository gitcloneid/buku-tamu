package com.hv.bukutm.presentation.screen.dashboard

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TanggalViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _appointments = mutableStateOf<List<Appointment>>(emptyList())
    val appointments: State<List<Appointment>> = _appointments

    private val _selectedDate = mutableStateOf<LocalDate?>(LocalDate.now())
    val selectedDate: State<LocalDate?> = _selectedDate

    private val _currentMonthStartDate = mutableStateOf(LocalDate.now().withDayOfMonth(1))
    val currentMonthStartDate: State<LocalDate> = _currentMonthStartDate

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage


    init {
        loadPendingAppointments()
    }

    private fun loadPendingAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            tokenManager.accessToken.firstOrNull()?.let { token ->
                val result = appointmentRepository.getPendingAppointments(token)
                result.onSuccess { fetchedAppointments ->
                    _appointments.value = fetchedAppointments
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _errorMessage.value = error.localizedMessage
                    _isLoading.value = false
                    Log.e("TanggalViewModel", "Error fetching appointments", error)
                }
            } ?: run {
                _errorMessage.value = "No token available"
                _isLoading.value = false
            }
        }
    }

    fun selectDate(date: LocalDate) {
        if (!date.isBefore(LocalDate.now())) {
            _selectedDate.value = date
        }
    }

    fun navigateToPreviousMonth() {
        val previousMonth = _currentMonthStartDate.value.minusMonths(1)
        if (!previousMonth.isBefore(LocalDate.now().withDayOfMonth(1))) {
            _currentMonthStartDate.value = previousMonth
        }
    }

    fun navigateToNextMonth() {
        _currentMonthStartDate.value = _currentMonthStartDate.value.plusMonths(1)
    }
}
