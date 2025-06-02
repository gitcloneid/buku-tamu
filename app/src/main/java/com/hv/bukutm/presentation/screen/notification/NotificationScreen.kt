package com.hv.bukutm.presentation.screen.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.hv.bukutm.domain.model.Notification
import com.hv.bukutm.presentation.utils.NotificationHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val realtimeNotification by viewModel.realtimeNotification.collectAsState()

    val notificationPermissionResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Izin diberikan
            } else {
                // Izin ditolak
            }
        }
    )

    LaunchedEffect(realtimeNotification) {
        realtimeNotification?.let {
            NotificationHelper.showNotification(
                context,
                title = "Buku Tamu Stemdasi",
                message = it.message
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Notifikasi",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }
            notifications.isEmpty() -> {
                Text("Tidak ada notifikasi tersimpan.")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification = notification)
                        Divider()
                    }
                }
            }
        }
    }

    // Request notification permission on composable creation (for Android 13+)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } 
}

@Composable
fun NotificationItem(notification: Notification) {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale("id", "ID"))

    val formattedWaktu = try {
        val dateTime = LocalDateTime.parse(notification.waktu, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        notification.waktu
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.pesan,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formattedWaktu,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}