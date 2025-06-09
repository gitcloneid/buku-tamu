package com.hv.bukutm.presentation.screen.dashboard

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.data.remote.WhatsAppApi
import com.hv.bukutm.data.remote.WhatsAppMediaRequest
import com.hv.bukutm.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class AddAppointmentUiState(
    val name: String = "",
    val nameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val date: String = "",
    val dateError: String? = null,
    val time: String = "",
    val timeError: String? = null,
    val purpose: String = "",
    val purposeError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val timeDialogError: String? = null,
    val dateDialogError: String? = null,
    val kodeQr: String? = null,
    val snackbarMessage: String? = null,
    val isQrSent: Boolean = false // Tambahan untuk melacak status pengiriman QR
)

@HiltViewModel
class AddAppointmentViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val appointmentRepository: AppointmentRepository,
    private val whatsAppApi: WhatsAppApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddAppointmentUiState())
    val uiState: StateFlow<AddAppointmentUiState> = _uiState.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()

    private val _timeDialogError = MutableStateFlow<String?>(null)
    val timeDialogError: StateFlow<String?> = _timeDialogError.asStateFlow()

    private val _dateDialogError = MutableStateFlow<String?>(null)
    val dateDialogError: StateFlow<String?> = _dateDialogError.asStateFlow()

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "Nama tidak boleh kosong" else null
            )
        }
    }

    fun updatePhone(phone: String) {
        _uiState.update {
            it.copy(
                phone = phone,
                phoneError = when {
                    phone.isBlank() -> "Nomor telepon tidak boleh kosong"
                    !phone.matches(Regex("^[0-9]{10,13}$")) -> "Nomor telepon tidak valid (10-13 digit)"
                    else -> null
                }
            )
        }
    }

    fun updateDate(date: LocalDate?) {
        val formattedDate = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
        _uiState.update {
            it.copy(
                date = formattedDate,
                dateError = if (date == null) "Tanggal harus dipilih" else null,
                time = "",
                timeError = null
            )
        }
        _dateDialogError.value = null
    }

    fun onDateSelected(date: LocalDate?) {
        val today = LocalDate.now()
        val maxDate = today.plusMonths(2)
        if (date == null) {
            _dateDialogError.value = null
            updateDate(null)
        } else if (date.isBefore(today)) {
            _dateDialogError.value = "Tanggal tidak boleh sebelum hari ini"
            Log.d("ViewModel", "Date Error: ${_dateDialogError.value}")
        } else if (date.isAfter(maxDate)) {
            _dateDialogError.value = "Tanggal tidak boleh lebih dari 2 bulan ke depan"
            Log.d("ViewModel", "Date Error: ${_dateDialogError.value}")
        } else {
            updateDate(date)
            showDatePicker(false)
            _dateDialogError.value = null
        }
    }

    fun dismissDatePicker() {
        showDatePicker(false)
        _dateDialogError.value = null
    }

    fun updateTime(time: LocalTime?) {
        val startTime = LocalTime.of(8, 0)
        val endTime = LocalTime.of(14, 0)
        _uiState.update {
            it.copy(
                time = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                timeError = when {
                    time == null -> "Waktu harus dipilih"
                    time.isBefore(startTime) || time.isAfter(endTime) -> "Waktu harus antara 08:00 - 14:00"
                    else -> null
                }
            )
        }
        _timeDialogError.value = null
    }

    fun onTimeSelected(hour: Int, minute: Int, isToday: Boolean = false) {
        val selectedTime = LocalTime.of(hour, minute)
        val startTime = LocalTime.of(8, 0)
        val endTime = LocalTime.of(14, 0)
        val currentTime = LocalTime.now()

        when {
            selectedTime.isBefore(startTime) || selectedTime.isAfter(endTime) -> {
                _timeDialogError.value = "Waktu harus antara 08:00 - 14:00"
            }
            isToday && selectedTime.isBefore(currentTime) -> {
                _timeDialogError.value = "Waktu tidak boleh kurang dari waktu sekarang"
            }
            else -> {
                updateTime(selectedTime)
                showTimePicker(false)
                _timeDialogError.value = null
            }
        }
    }

    fun dismissTimePicker() {
        showTimePicker(false)
        _timeDialogError.value = null
    }

    fun updatePurpose(purpose: String) {
        _uiState.update {
            it.copy(
                purpose = purpose,
                purposeError = if (purpose.isBlank()) "Keperluan tidak boleh kosong" else null
            )
        }
    }

    fun showDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }

    fun showTimePicker(show: Boolean) {
        _showTimePicker.value = show
    }

    private fun validate(): Boolean {
        var isValid = true
        val nameError = if (_uiState.value.name.isBlank()) "Nama tidak boleh kosong" else null
        val phoneError = when {
            _uiState.value.phone.isBlank() -> "Nomor telepon tidak boleh kosong"
            !_uiState.value.phone.matches(Regex("^[0-9]{10,13}$")) -> "Nomor telepon tidak valid (10-13 digit)"
            else -> null
        }
        val dateError = if (_uiState.value.date.isBlank()) "Tanggal harus dipilih" else null
        val timeError = _uiState.value.run {
            when {
                time.isBlank() -> "Waktu harus dipilih"
                else -> {
                    try {
                        val selectedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                        val startTime = LocalTime.of(8, 0)
                        val endTime = LocalTime.of(14, 0)
                        val currentTime = LocalTime.now()
                        val today = LocalDate.now()
                        val selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                        when {
                            selectedTime.isBefore(startTime) || selectedTime.isAfter(endTime) -> {
                                "Waktu harus antara 08:00 - 14:00"
                            }
                            selectedDate.isEqual(today) && selectedTime.isBefore(currentTime) -> {
                                "Waktu tidak boleh kurang dari waktu sekarang"
                            }
                            else -> null
                        }
                    } catch (e: Exception) {
                        "Format waktu tidak valid"
                    }
                }
            }
        }
        val purposeError = if (_uiState.value.purpose.isBlank()) "Keperluan tidak boleh kosong" else null

        _uiState.update {
            it.copy(
                nameError = nameError,
                phoneError = phoneError,
                dateError = dateError,
                timeError = timeError,
                purposeError = purposeError
            )
        }

        if (nameError != null || phoneError != null || dateError != null || timeError != null || purposeError != null) {
            isValid = false
        }
        return isValid
    }

    fun submitAppointment(onSuccess: (String) -> Unit) {
        if (!validate()) {
            _uiState.update { it.copy(error = "Harap perbaiki kesalahan di atas") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenManager.accessToken.firstOrNull()
                if (token == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Token tidak ditemukan, silakan login kembali") }
                    return@launch
                }

                val dateForApi = try {
                    val inputDate = LocalDate.parse(_uiState.value.date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    inputDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Format tanggal tidak valid") }
                    return@launch
                }

                val tamuResponse = appointmentRepository.createTamu(
                    token = token,
                    nama = _uiState.value.name,
                    telepon = _uiState.value.phone
                )

                tamuResponse.fold(
                    onSuccess = { tamu ->
                        val appointmentResponse = appointmentRepository.createAppointment(
                            token = token,
                            idTamu = tamu.idTamu,
                            tanggal = dateForApi,
                            waktu = _uiState.value.time,
                            keperluan = _uiState.value.purpose
                        )

                        appointmentResponse.fold(
                            onSuccess = { kodeQr ->
                                _uiState.update { it.copy(isLoading = false, error = null, kodeQr = kodeQr) }
                                onSuccess(kodeQr)
                            },
                            onFailure = { error ->
                                _uiState.update { it.copy(isLoading = false, error = "Gagal membuat janji temu: ${error.message}") }
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, error = "Gagal menambahkan tamu: ${error.message}") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Terjadi kesalahan: ${e.message}") }
            }
        }
    }

    fun setTimeDialogError(message: String) {
        _timeDialogError.value = message
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun sendQrToWhatsApp(bitmap: Bitmap, phoneNumber: String) {
        viewModelScope.launch {
            try {
                // Convert Bitmap to Base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP) // Avoid line breaks

                // Log Base64 string for debugging
                Log.d("ViewModel", "Base64 length: ${base64Image.length}, content: $base64Image")

                // Validate Base64 string
                if (!base64Image.matches(Regex("^[A-Za-z0-9+/=]+$"))) {
                    _uiState.update { it.copy(snackbarMessage = "Base64 string contains invalid characters") }
                    Log.e("ViewModel", "Invalid Base64 string: $base64Image")
                    return@launch
                }

                // Normalize phone number
                val normalizedPhone = when {
                    phoneNumber.startsWith("0") -> "+62${phoneNumber.substring(1)}"
                    !phoneNumber.startsWith("+") -> "+62$phoneNumber"
                    else -> phoneNumber
                }

                val formattedDate = try {
                    val date = LocalDate.parse(_uiState.value.date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID")))
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error formatting date: ${e.message}")
                    _uiState.value.date // Fallback to original format
                }

                // Modified WhatsApp message
                val caption = """
                    Kode QR Janji Temu
                    Nama: ${_uiState.value.name}
                    Tanggal: $formattedDate
                    Waktu: ${_uiState.value.time}
                    Kode: *${_uiState.value.kodeQr}*
                    Silakan tunjukkan QR code ini saat kedatangan.
                    Untuk memeriksa status, gunakan aplikasi Buku Tamu SMKN 2 Singosari.
                """.trimIndent()

                // Send to WhatsApp API
                val response = whatsAppApi.sendMedia(
                    WhatsAppMediaRequest(
                        to = normalizedPhone,
                        mediaBase64 = base64Image,
                        filename = "appointment_qr.png",
                        caption = caption
                    )
                )

                if (response.status == "media_sent") {
                    _uiState.update { it.copy(snackbarMessage = "Kode QR berhasil dikirim ke WhatsApp", isQrSent = true) }
                    Log.d("ViewModel", "QR code sent successfully to $normalizedPhone")
                } else {
                    _uiState.update { it.copy(snackbarMessage = "Gagal mengirim QR ke WhatsApp: ${response.status}") }
                    Log.e("ViewModel", "Failed to send QR code: ${response.status}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Error mengirim QR ke WhatsApp: ${e.message}") }
                Log.e("ViewModel", "Error sending QR code: ${e.message}")
            }
        }
    }
}