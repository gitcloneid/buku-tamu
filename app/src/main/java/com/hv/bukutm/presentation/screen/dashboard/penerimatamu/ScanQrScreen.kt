package com.hv.bukutm.activity

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.presentation.scanner.VerticalCaptureActivity
import com.hv.bukutm.presentation.screen.dashboard.penerimatamu.ScanQrViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.util.*

@Composable
fun ScanQrScreen(
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val nestedNavController = rememberNavController()
    val viewModel: ScanQrViewModel = hiltViewModel()
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.fetchAppointmentByQr(result.contents)
            if (viewModel.errorMessage.value == null) {
                nestedNavController.navigate("result")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVerticalScan(scanLauncher)
        } else {
            showPermissionDeniedDialog = true
        }
    }

    LaunchedEffect(viewModel.appointment.value, viewModel.errorMessage.value) {
        if (viewModel.appointment.value != null && viewModel.errorMessage.value == null) {
            nestedNavController.navigate("result")
        }
    }

    NavHost(
        navController = nestedNavController,
        startDestination = "scanner",
        modifier = modifier.fillMaxSize()
    ) {
        composable("scanner") {
            ScannerContent(
                onScanRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                errorMessage = viewModel.errorMessage.value,
                onDismissError = { viewModel.clearError() },
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable("result") {
            val appointment = viewModel.appointment.value
            val errorMessage = viewModel.errorMessage.value

            if (appointment != null && errorMessage == null) {
                AppointmentResultScreen(
                    appointment = appointment,
                    onBackPressed = { nestedNavController.popBackStack() },
                    viewModel = viewModel,
                    isLoading = viewModel.isLoading.value
                )
            } else {
                LaunchedEffect(Unit) {
                    nestedNavController.popBackStack("scanner", inclusive = false)
                }
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Izin Kamera Diperlukan", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Aplikasi memerlukan izin kamera untuk memindai kode QR. Silakan izinkan di pengaturan aplikasi.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ScannerContent(
    onScanRequest: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(3 / 4f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                ScannerFrameWithAnimation()
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pindai Kode QR",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Posisikan kode QR dalam bingkai untuk memindai",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onScanRequest,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp)
                    .shadow(4.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mulai Pemindaian",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        errorMessage?.let {
            AlertDialog(
                onDismissRequest = onDismissError,
                title = { Text("Perhatian", style = MaterialTheme.typography.titleMedium) },
                text = { Text(it) },
                confirmButton = {
                    TextButton(onClick = onDismissError) {
                        Text("OK", fontWeight = FontWeight.SemiBold)
                    }
                },
                modifier = Modifier.align(Alignment.Center),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ScannerFrameWithAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        ScannerFrameVisual()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(2.dp)
                .offset(y = ((0.8f * scanOffset - 0.4f) * 300).dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ScannerFrameVisual() {
    Box(
        modifier = Modifier
            .fillMaxSize(0.9f)
            .aspectRatio(3 / 4f)
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        val cornerSize = 28.dp
        val cornerThickness = 4.dp
        val cornerColor = MaterialTheme.colorScheme.primary

        // Top-left corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .clip(RoundedCornerShape(topStart = 4.dp))
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .clip(RoundedCornerShape(topStart = 4.dp))
                .background(cornerColor)
        )

        // Top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .clip(RoundedCornerShape(topEnd = 4.dp))
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .clip(RoundedCornerShape(topEnd = 4.dp))
                .background(cornerColor)
        )

        // Bottom-left corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .clip(RoundedCornerShape(bottomStart = 4.dp))
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .clip(RoundedCornerShape(bottomStart = 4.dp))
                .background(cornerColor)
        )

        // Bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .clip(RoundedCornerShape(bottomEnd = 4.dp))
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .clip(RoundedCornerShape(bottomEnd = 4.dp))
                .background(cornerColor)
        )
    }
}

private fun startVerticalScan(launcher: androidx.activity.result.ActivityResultLauncher<ScanOptions>) {
    val options = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt("Pindai kode QR jadwal temu")
        setCameraId(0)
        setBeepEnabled(true)
        setOrientationLocked(true)
        setBarcodeImageEnabled(true)
        captureActivity = VerticalCaptureActivity::class.java
    }
    launcher.launch(options)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentResultScreen(
    appointment: Appointment,
    onBackPressed: () -> Unit,
    viewModel: ScanQrViewModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Janji Temu",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
//                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            val statusColor = when (appointment.status) {
                "Menunggu" -> Color(0xFF00729F)
                "Selesai" -> Color(0xFF148E00)
                "Telat" -> Color(0xFFF25C05)
                else -> MaterialTheme.colorScheme.primary
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    val customStatus = when (appointment.status) {
                        "Telat" -> "Terlambat"
                        else -> appointment.status
                    }
                    Text(
                        text = "Status: $customStatus",
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppointmentDetailCard(
                appointment = appointment,
                viewModel = viewModel,
                isLoading = isLoading,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentDetailCard(
    appointment: Appointment,
    viewModel: ScanQrViewModel,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var lastRescheduleStatus by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(lastRescheduleStatus) {
        if (lastRescheduleStatus == true) {
            snackbarHostState.showSnackbar(
                message = "Janji temu berhasil dijadwalkan ulang",
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            lastRescheduleStatus = null
        }
    }

    LaunchedEffect(viewModel.updateStatusSuccess.value) {
        viewModel.updateStatusSuccess.value?.let { success ->
            if (success) {
                snackbarHostState.showSnackbar(
                    message = "Status berhasil diperbarui",
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearUpdateStatus()
            }
        }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Informasi Tamu
            SectionTitle("Informasi Tamu")
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Nama", value = appointment.tamu.nama)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Telepon", value = appointment.tamu.telepon)

            DividerSection()

            // Informasi Guru
            SectionTitle("Informasi Guru")
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Nama Guru", value = appointment.guru.nama)

            DividerSection()

            // Waktu Janji Temu
            SectionTitle("Waktu Janji Temu")
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Tanggal", value = appointment.tanggal.formatToIndoDate())
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Waktu", value = appointment.waktu.formatToTime())

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (appointment.status == "Menunggu") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reschedule Button
                    OutlinedButton(
                        onClick = { showRescheduleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Jadwal ulang",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Set Telat Button
                        Button(
                            onClick = { viewModel.updateAppointmentStatus(appointment.idJanjiTemu, "Telat") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF25C05).copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                "Terlambat",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }

                        // Set Selesai Button
                        Button(
                            onClick = { viewModel.updateAppointmentStatus(appointment.idJanjiTemu, "Selesai") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF148E00).copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                "Selesai",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                val customStatus = when (appointment.status) {
                    "Telat" -> "Terlambat"
                    else -> appointment.status
                }
                Text(
                    text = "Status: ${customStatus}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (appointment.status) {
                        "Selesai" -> Color(0xFF148E00)
                        "Telat" -> Color(0xFFF25C05)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            when (appointment.status) {
                                "Selesai" -> Color(0xFF148E00).copy(alpha = 0.1f)
                                "Telat" -> Color(0xFFF25C05).copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    val scope = rememberCoroutineScope()
    if (showRescheduleDialog) {
        RescheduleDialog(
            appointmentId = appointment.idJanjiTemu,
            initialDate = appointment.tanggal,
            initialTime = appointment.waktu,
            viewModel = viewModel,
            onDismissRequest = { showRescheduleDialog = false },
            onRescheduleSuccess = {
                scope.launch {
                    lastRescheduleStatus = true //trigger launched eff
                    snackbarHostState.showSnackbar(
                        message = "Janji temu berhasil dijadwalkan ulang",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                    delay(2000)
                    showRescheduleDialog = false
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun DividerSection() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescheduleDialog(
    appointmentId: Int,
    initialDate: String,
    initialTime: String,
    viewModel: ScanQrViewModel,
    onDismissRequest: () -> Unit,
    onRescheduleSuccess: () -> Unit
) {
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var dateDialogError by remember { mutableStateOf<String?>(null) }
    var timeDialogError by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val maxDate = today.plusMonths(1)
    val currentTime = LocalTime.now()
    val isSelectedDateToday = try {
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).isEqual(today)
    } catch (e: Exception) { false }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        } catch (e: Exception) {
            today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selectedDate = LocalDate.ofEpochDay(utcTimeMillis / (1000 * 86400))
                return !selectedDate.isBefore(today) && !selectedDate.isAfter(maxDate)
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year in today.year..maxDate.year
            }
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")).hour
        } catch (e: Exception) { 8 },
        initialMinute = try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")).minute
        } catch (e: Exception) { 0 }
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Jadwal Ulang Janji Temu",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Tanggal") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true },
                    enabled = false,
                    isError = dateError != null,
                    supportingText = {
                        dateError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    label = {
                        Text(
                            if (isSelectedDateToday)
                                "Waktu (Min: ${currentTime.hour.toString().padStart(2, '0')}:${currentTime.minute.toString().padStart(2, '0')})"
                            else
                                "Waktu (08:00 - 14:00)"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (date.isNotBlank()) showTimePicker = true },
                    enabled = false,
                    isError = timeError != null,
                    supportingText = {
                        when {
                            date.isBlank() -> Text(
                                "Pilih tanggal terlebih dahulu",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            timeError != null -> Text(
                                timeError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            "Batal",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = {
                            dateError = if (date.isBlank()) "Tanggal harus dipilih" else null
                            timeError = when {
                                time.isBlank() -> "Waktu harus dipilih"
                                else -> {
                                    try {
                                        val selectedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                                        val startTime = LocalTime.of(8, 0)
                                        val endTime = LocalTime.of(14, 0)
                                        when {
                                            selectedTime.isBefore(startTime) || selectedTime.isAfter(endTime) ->
                                                "Waktu harus antara 08:00 - 14:00"
                                            isSelectedDateToday && selectedTime.isBefore(currentTime) ->
                                                "Waktu tidak boleh kurang dari waktu sekarang"
                                            else -> null
                                        }
                                    } catch (e: Exception) {
                                        "Format waktu tidak valid"
                                    }
                                }
                            }

                            if (dateError == null && timeError == null) {
                                try {
                                    val apiDate = LocalDate.parse(
                                        date,
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    viewModel.rescheduleAppointment(
                                        appointmentId,
                                        apiDate,
                                        time
                                    ) { success ->
                                        if (success) {
                                            onRescheduleSuccess()
                                            onDismissRequest()
                                        }
                                    }
                                } catch (e: Exception) {
                                    dateError = "Format tanggal tidak valid"
                                }
                            }
                        },
                        enabled = !viewModel.isLoading.value,
                        modifier = Modifier
                            .height(48.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Simpan",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false; dateDialogError = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = LocalDate.ofEpochDay(it / (1000 * 86400))
                        if (selectedDate.isBefore(today)) {
                            dateDialogError = "Tanggal tidak boleh sebelum hari ini"
                        } else if (selectedDate.isAfter(maxDate)) {
                            dateDialogError = "Tanggal tidak boleh lebih dari 1 bulan ke depan"
                        } else {
                            date = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            dateError = null
                            showDatePicker = false
                            dateDialogError = null
                            time = "" // Reset time when date changes
                        }
                    }
                }) {
                    Text("OK", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false; dateDialogError = null }) {
                    Text("Batal", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
                dateDialogError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false; timeDialogError = null },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val startTime = LocalTime.of(8, 0)
                    val endTime = LocalTime.of(14, 0)
                    when {
                        selectedTime.isBefore(startTime) || selectedTime.isAfter(endTime) ->
                            timeDialogError = "Waktu harus antara 08:00 - 14:00"
                        isSelectedDateToday && selectedTime.isBefore(currentTime) ->
                            timeDialogError = "Waktu tidak boleh kurang dari waktu sekarang"
                        else -> {
                            time = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                            timeError = null
                            showTimePicker = false
                            timeDialogError = null
                        }
                    }
                }) {
                    Text("OK", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false; timeDialogError = null }) {
                    Text("Batal", fontWeight = FontWeight.SemiBold)
                }
            },
            title = { Text("Pilih Waktu", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    )
                    timeDialogError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = ":",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

fun String.formatToTime(): String {
    return try {
        val time = LocalTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        time.format(formatter)
    } catch (e: Exception) {
        this
    }
}

fun String.formatToIndoDate(): String {
    return try {
        val date = LocalDate.parse(this)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))
        date.format(formatter)
    } catch (e: Exception) {
        this
    }
}