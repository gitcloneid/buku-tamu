package com.hv.bukutm.presentation.screen.dashboard.penerimatamu

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.R
import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.presentation.scanner.VerticalCaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScanQrScreen(
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val nestedNavController = rememberNavController()
    val viewModel: ScanQrViewModel = hiltViewModel()
    val context = LocalContext.current
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.fetchAppointmentByQr(result.contents)
            // Only navigate to result if there's no error
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

    // Listen for changes to appointment or error state
    LaunchedEffect(viewModel.appointment.value, viewModel.errorMessage.value) {
        if (viewModel.appointment.value != null && viewModel.errorMessage.value == null) {
            nestedNavController.navigate("result")
        }
    }

    NavHost(
        navController = nestedNavController,
        startDestination = "scanner",
        modifier = modifier
            .fillMaxSize()
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

            // Only show appointment screen if appointment exists and no error
            if (appointment != null && errorMessage == null) {
                AppointmentResultScreen(
                    appointment = appointment,
                    onBackPressed = { nestedNavController.popBackStack() },
                    viewModel = viewModel,
                    isLoading = viewModel.isLoading.value
                )
            } else {
                // If there's an error or no appointment, navigate back to scanner
                LaunchedEffect(Unit) {
                    nestedNavController.popBackStack("scanner", inclusive = false)
                }
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    // Permission denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Izin Kamera Diperlukan") },
            text = { Text("Aplikasi memerlukan izin kamera untuk memindai kode QR. Silakan izinkan di pengaturan aplikasi.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK")
                }
            }
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanner frame visual indicator
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(3 / 4f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                ScannerFrameWithIcon()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Posisikan kode QR dalam bingkai untuk memindai",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scan button
            Button(
                onClick = onScanRequest,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.8f),
                shape = RoundedCornerShape(28.dp),
            ) {
                Text(
                    text = "Mulai Pemindaian",
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Error dialog
        errorMessage?.let {
            AlertDialog(
                onDismissRequest = onDismissError,
                title = { Text("Perhatian") },
                text = { Text(it) },
                confirmButton = {
                    TextButton(onClick = onDismissError) {
                        Text("OK")
                    }
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ScannerFrameWithIcon() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add some padding around the frame
        contentAlignment = Alignment.Center
    ) {
        ScannerFrameVisual()
        Icon(
            painter = painterResource(id = R.drawable.qr_scan_svgrepo_com),
            contentDescription = "Scan Icon",
            modifier = Modifier.size(280.dp), // Adjust the size of the icon
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) // Optional tint
        )
    }
}

@Composable
private fun ScannerFrameVisual() {
    Box(
        modifier = Modifier
            .fillMaxSize(0.9f) // Make the frame slightly smaller than the container
            .aspectRatio(3 / 4f) // Maintain the aspect ratio
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        val cornerSize = 24.dp
        val cornerThickness = 4.dp
        val cornerColor = MaterialTheme.colorScheme.primary

        // Top-left corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .background(cornerColor)
        )

        // Top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((cornerThickness / 2), (-cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .background(cornerColor)
        )

        // Bottom-left corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
                .background(cornerColor)
        )

        // Bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerSize, height = cornerThickness)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((cornerThickness / 2), (cornerThickness / 2))
                .size(width = cornerThickness, height = cornerSize)
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
    modifier: Modifier = Modifier,
    isScanSuccessful: Boolean = false // Parameter baru untuk menentukan apakah pemindaian berhasil
) {
    Scaffold(
        topBar = {
            if (isScanSuccessful) {
                // TopAppBar saat pemindaian berhasil
                TopAppBar(
                    title = { Text("Detail Janji Temu") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            } else {
                // TopAppBar saat pemindaian gagal atau belum berhasil
                TopAppBar(
                    title = { Text("Pemindaian QR Code") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    windowInsets = WindowInsets(0, 0 ,0 ,0)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            // Status indicator
            val statusColor = when (appointment.status) {
                "Menunggu" -> Color(0xFFFF9800)
                "Selesai" -> Color(0xFF4CAF50)
                "Telat" -> Color(0xFFEC430E)
                else -> MaterialTheme.colorScheme.primary
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, statusColor)
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
                    Text(
                        text = "Status: ${appointment.status}",
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appointment details
            AppointmentDetailCard(
                appointment = appointment,
                viewModel = viewModel,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun AppointmentDetailCard(
    appointment: Appointment,
    viewModel: ScanQrViewModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
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
            // Guest information section
            Text(
                text = "Informasi Tamu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Nama", value = appointment.tamu.nama)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Telepon", value = appointment.tamu.telepon)

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Teacher information section
            Text(
                text = "Informasi Guru",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Nama Guru", value = appointment.guru.nama)

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Appointment time information
            Text(
                text = "Waktu Janji Temu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "Tanggal", value = appointment.tanggal.formatToIndoDate())
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Waktu", value = appointment.waktu.formatToTime())

            Spacer(modifier = Modifier.height(24.dp))

            // Status update buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.updateAppointmentStatus(appointment.idJanjiTemu, "Telat") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    enabled = !isLoading && appointment.status == "Menunggu",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEC430E)
                    )
                ) {
                    Text("Set Telat")
                }
                Button(
                    onClick = { viewModel.updateAppointmentStatus(appointment.idJanjiTemu, "Selesai") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    enabled = !isLoading && appointment.status == "Menunggu",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Set Selesai")
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Display error if exists (during status update)
            viewModel.errorMessage.value?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
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
        this // fallback kalau parsing gagal
    }
}