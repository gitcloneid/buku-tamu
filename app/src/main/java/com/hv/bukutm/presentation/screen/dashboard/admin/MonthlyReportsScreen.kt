package com.hv.bukutm.presentation.screen.dashboard.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hv.bukutm.data.api.MonthlyReport
import com.hv.bukutm.data.api.WeeklyStats
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    navController: NavController,
    viewModel: MonthlyReportViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.value
    val selectedMonth = remember { mutableStateOf(uiState.selectedMonth) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val months = (1..12).map {
        viewModel.getMonthName(it).replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
        }
    }

    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Bulanan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Month Selector Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pilih Bulan",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.getMonthName(selectedMonth.value)
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                        else it.toString()
                                    },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                months.forEachIndexed { index, month ->
                                    DropdownMenuItem(
                                        text = { Text(month) },
                                        onClick = {
                                            selectedMonth.value = index + 1
                                            viewModel.fetchMonthlyReport(index + 1)
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { expandedDropdown = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pilih bulan"
                            )
                        }
                    }
                }
            }

            // Export Button
            if (uiState.monthlyReport != null && uiState.monthlyReport.totalAppointments > 0) {
                Button(
                    onClick = {
                        exportToExcel(
                            context,
                            uiState.monthlyReport,
                            viewModel.getMonthName(uiState.selectedMonth),
                            uiState.selectedMonth
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export ke Excel")
                }
            }

            // Monthly Report Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    uiState.error != null -> Text(
                        text = uiState.error ?: "Terjadi kesalahan",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    uiState.monthlyReport == null || uiState.monthlyReport.totalAppointments == 0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tidak ada data laporan di bulan ini",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> MonthlyReportContent(report = uiState.monthlyReport)
                }
            }
        }
    }
}

@Composable
private fun MonthlyReportContent(report: MonthlyReport) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ringkasan Bulanan",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Total Janji Temu", value = report.totalAppointments.toString())
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Tingkat Penyelesaian", value = "${report.completionRate}%")
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Statistik Mingguan",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomBarChart(weeklyStats = report.weeklyStats)
            }
        }
    }
}

@Composable
private fun CustomBarChart(weeklyStats: List<WeeklyStats>) {
    // Colors - blue for total background, green for completed
    val totalColor = Color(0xFF2196F3) // Blue for uncompleted portion
    val completedColor = Color(0xFF4CAF50) // Green for completed portion
    val maxStatValue = weeklyStats.maxOfOrNull { it.total }?.toFloat() ?: 1f
    val yAxisMax = (ceil(maxStatValue / 10) * 10).toInt().takeIf { it > 0 } ?: 10

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(280.dp)) { // Increased height for better visibility
            // Y-axis
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = yAxisMax.toString(), style = MaterialTheme.typography.labelSmall)
                Text(text = (yAxisMax / 2).toString(), style = MaterialTheme.typography.labelSmall)
                Text(text = "0", style = MaterialTheme.typography.labelSmall)
            }

            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEach { stat ->
                    StackedBar(
                        totalValue = stat.total.toFloat(),
                        completedValue = stat.completed.toFloat(),
                        maxValue = yAxisMax.toFloat(),
                        totalColor = totalColor,
                        completedColor = completedColor,
                        label = "M${stat.weekNumber}"
                    )
                }
            }
        }
        Divider(modifier = Modifier.fillMaxWidth().padding(start = 40.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = totalColor, text = "Total Janji Temu")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = completedColor, text = "Selesai")
        }
    }
}

@Composable
private fun StackedBar(
    totalValue: Float,
    completedValue: Float,
    maxValue: Float,
    totalColor: Color,
    completedColor: Color,
    label: String
) {
    val barWidth = 48.dp // Slightly wider bars
    val totalHeightFraction = if (maxValue > 0) totalValue / maxValue else 0f
    val completionRate = if (totalValue > 0) (completedValue / totalValue * 100).toInt() else 0
    val uncompletedValue = totalValue - completedValue

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {


        // Stacked Bar
        Column(
            modifier = Modifier
                .width(barWidth)
                .fillMaxHeight(totalHeightFraction),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Uncompleted portion (blue) - top part
            if (uncompletedValue > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((200.dp * totalHeightFraction * (uncompletedValue / totalValue)))
                        .clip(
                            if (completedValue == 0f) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            else RoundedCornerShape(0.dp)
                        )
                        .background(totalColor)
                ) {
                    // Show total number in the middle of uncompleted portion if it's large enough
                    if (uncompletedValue >= totalValue * 0.3f) {
                        Text(
                            text = totalValue.toInt().toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Completed portion (green) - bottom part
            if (completedValue > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((200.dp * totalHeightFraction * (completedValue / totalValue)))
                        .clip(
                            if (uncompletedValue == 0f) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            else RoundedCornerShape(0.dp)
                        )
                        .background(completedColor)
                ) {
                    // Show completed number in the middle of completed portion if it's large enough
                    if (completedValue >= totalValue * 0.3f || uncompletedValue == 0f) {
                        Text(
                            text = completedValue.toInt().toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // If total is shown in uncompleted but uncompleted is small, show total on top
            if (totalValue > 0 && uncompletedValue < totalValue * 0.3f && uncompletedValue > 0) {
                Text(
                    text = totalValue.toInt().toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .offset(y = (-8).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Week label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun exportToExcel(
    context: Context,
    report: MonthlyReport,
    monthName: String,
    monthNumber: Int
) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Laporan Bulanan")
        sheet.setColumnWidth(0, 15 * 256)
        sheet.setColumnWidth(1, 15 * 256)
        sheet.setColumnWidth(2, 15 * 256)
        sheet.setColumnWidth(3, 20 * 256)
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Laporan Bulanan - $monthName ${report.year}")
        val summaryRow1 = sheet.createRow(2)
        summaryRow1.createCell(0).setCellValue("Total Janji Temu")
        summaryRow1.createCell(1).setCellValue(report.totalAppointments.toString())
        val summaryRow2 = sheet.createRow(3)
        summaryRow2.createCell(0).setCellValue("Tingkat Penyelesaian")
        summaryRow2.createCell(1).setCellValue("${report.completionRate}%")
        val weeklyHeaderRow = sheet.createRow(5)
        weeklyHeaderRow.createCell(0).setCellValue("Minggu")
        weeklyHeaderRow.createCell(1).setCellValue("Total")
        weeklyHeaderRow.createCell(2).setCellValue("Selesai")
        weeklyHeaderRow.createCell(3).setCellValue("Tingkat Penyelesaian")
        report.weeklyStats.forEachIndexed { index, stat ->
            val weekRow = sheet.createRow(6 + index)
            weekRow.createCell(0).setCellValue(stat.weekNumber.toString())
            weekRow.createCell(1).setCellValue(stat.total.toString())
            weekRow.createCell(2).setCellValue(stat.completed.toString())
            val completionRate = if (stat.total > 0) (stat.completed.toFloat() / stat.total.toFloat()) * 100 else 0f
            weekRow.createCell(3).setCellValue("${"%.1f".format(completionRate)}%")
        }
        val fileName = "BukuTamu_${monthName}_${report.year}_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)
        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)
        outputStream.close()
        workbook.close()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan laporan"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal mengekspor laporan: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}