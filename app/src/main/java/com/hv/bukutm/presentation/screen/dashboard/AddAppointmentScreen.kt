package com.hv.bukutm.presentation.screen.dashboard

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

fun generateQrCodeBitmap(data: String?, width: Int, height: Int): Bitmap? {
    if (data.isNullOrEmpty()) return null
    val hints = mutableMapOf<EncodeHintType, Any>()
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    val writer = QRCodeWriter()
    return try {
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    navController: NavController,
    viewModel: AddAppointmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val maxDate = remember { today.plusMonths(2) }
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val showTimePicker by viewModel.showTimePicker.collectAsState()
    val dateDialogError by viewModel.dateDialogError.collectAsState()
    val timeDialogError by viewModel.timeDialogError.collectAsState()

    // Check if selected date is today
    val isSelectedDateToday = remember(uiState.date) {
        if (uiState.date.isNotBlank()) {
            try {
                val selectedDate = LocalDate.parse(uiState.date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                selectedDate.isEqual(today)
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    // Check if date is selected
    val isDateSelected = uiState.date.isNotBlank()

    // Get current time
    val currentTime = remember { LocalTime.now() }

    // Enhanced logic for time field availability
    val isTimeFieldAvailable = remember(uiState.date, currentTime) {
        when {
            !isDateSelected -> false
            isSelectedDateToday -> currentTime.isBefore(LocalTime.of(14, 0))
            else -> true
        }
    }

    // Configure date picker with restrictions
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selectedDate = LocalDate.ofEpochDay(utcTimeMillis / (1000 * 60 * 60 * 24))
                return !selectedDate.isBefore(today) && !selectedDate.isAfter(maxDate)
            }

            override fun isSelectableYear(year: Int): Boolean {
                val currentYear = today.year
                val maxYear = maxDate.year
                return year in currentYear..maxYear
            }
        },
        yearRange = today.year..maxDate.year
    )

    val timePickerState = rememberTimePickerState()

    val showQrDialog = remember { mutableStateOf(false) }
    val qrBitmap = remember(uiState.kodeQr) {
        generateQrCodeBitmap(uiState.kodeQr, 350, 350)?.asImageBitmap()
    }
    val qrBitmapForSend = remember(uiState.kodeQr) {
        generateQrCodeBitmap(uiState.kodeQr, 350, 350)
    }

    LaunchedEffect(uiState.kodeQr) {
        if (!uiState.kodeQr.isNullOrEmpty()) {
            showQrDialog.value = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Buat Janji Temu",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Isi Detail Janji Temu",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Nama Tamu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        isError = uiState.nameError != null,
                        supportingText = {
                            if (uiState.nameError != null) {
                                Text(
                                    uiState.nameError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = { Text("Nomor Telepon") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = uiState.phoneError != null,
                        supportingText = {
                            if (uiState.phoneError != null) {
                                Text(
                                    uiState.phoneError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = uiState.date,
                        onValueChange = {},
                        label = { Text("Tanggal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.showDatePicker(true) },
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Transparent,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = uiState.dateError != null,
                        supportingText = {
                            if (uiState.dateError != null) {
                                Text(
                                    uiState.dateError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = uiState.time,
                        onValueChange = {},
                        label = {
                            Text(
                                when {
                                    !isDateSelected -> "Pilih tanggal terlebih dahulu"
                                    isSelectedDateToday && currentTime.hour >= 14 -> "Waktu tidak tersedia (lewat jam 14:00)"
                                    isSelectedDateToday && !isTimeFieldAvailable -> "Waktu tidak tersedia untuk hari ini"
                                    isSelectedDateToday -> "Waktu (Min: ${currentTime.hour.toString().padStart(2, '0')}:${currentTime.minute.toString().padStart(2, '0')})"
                                    else -> "Waktu (08:00 - 14:00)"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isTimeFieldAvailable) {
                                    Modifier.clickable { viewModel.showTimePicker(true) }
                                } else {
                                    Modifier
                                }
                            ),
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (isTimeFieldAvailable)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            disabledLabelColor = if (isTimeFieldAvailable)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            disabledContainerColor = Color.Transparent,
                            disabledBorderColor = if (isTimeFieldAvailable)
                                MaterialTheme.colorScheme.outline
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
                        ),
                        isError = uiState.timeError != null,
                        supportingText = {
                            when {
                                !isDateSelected -> Text(
                                    "Pilih tanggal terlebih dahulu",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                isSelectedDateToday && currentTime.hour >= 14 -> Text(
                                    "Waktu sudah tidak tersedia untuk hari ini (lewat jam 14:00)",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                isSelectedDateToday && !isTimeFieldAvailable -> Text(
                                    "Tidak ada waktu yang tersedia pada hari ini",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                uiState.timeError != null -> Text(
                                    uiState.timeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = uiState.purpose,
                        onValueChange = { viewModel.updatePurpose(it) },
                        label = { Text("Keperluan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        isError = uiState.purposeError != null,
                        supportingText = {
                            if (uiState.purposeError != null) {
                                Text(
                                    uiState.purposeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.submitAppointment {
                            // Navigation will happen after QR dialog is closed
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    ),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Menyimpan...", style = MaterialTheme.typography.labelLarge)
                    }
                }
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        "Simpan Janji Temu",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            AnimatedVisibility(
                visible = showDatePicker,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                DatePickerDialog(
                    onDismissRequest = { viewModel.dismissDatePicker() },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val selectedDate = LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24))
                                viewModel.onDateSelected(selectedDate)
                            } ?: viewModel.dismissDatePicker()
                        }) {
                            Text("OK", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDatePicker() }) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                disabledDayContentColor = Color.Gray
                            ),
                            title = {
                                Text(
                                    text = "Pilih Tanggal",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            },
                            headline = {
                                Text(
                                    text = "Tanggal yang tersedia: ${today.dayOfMonth}/${today.monthValue}/${today.year} - ${maxDate.dayOfMonth}/${maxDate.monthValue}/${maxDate.year}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        )
                        if (!dateDialogError.isNullOrEmpty()) {
                            Text(
                                text = dateDialogError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showTimePicker,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissTimePicker() },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                viewModel.onTimeSelected(timePickerState.hour, timePickerState.minute, isSelectedDateToday)
                            }
                        ) {
                            Text("OK", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissTimePicker() }) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    title = {
                        Text(
                            if (isSelectedDateToday)
                                "Pilih Waktu (Min: ${currentTime.hour.toString().padStart(2, '0')}:${currentTime.minute.toString().padStart(2, '0')})"
                            else
                                "Pilih Waktu (08:00 - 14:00)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors()
                            )
                            if (!timeDialogError.isNullOrEmpty()) {
                                Text(
                                    text = timeDialogError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            AnimatedVisibility(
                visible = showQrDialog.value && qrBitmap != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Dialog(onDismissRequest = { showQrDialog.value = false; navController.navigateUp() }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .shadow(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Berhasil Ditambahkan!",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Mohon simpan kode QR ini dengan mengambil screenshot atau kirim ke WhatsApp Anda.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            qrBitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = "Kode QR Janji Temu",
                                    modifier = Modifier
                                        .size(300.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .padding(8.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        qrBitmapForSend?.let {
                                            viewModel.sendQrToWhatsApp(it, uiState.phone)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    "Kirim ke WhatsApp",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Button(
                                onClick = { showQrDialog.value = false; navController.navigateUp() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    "OK",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            if (uiState.error != null) {
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}