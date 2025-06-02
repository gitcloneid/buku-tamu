package com.hv.bukutm.presentation.screen.dashboard

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hv.bukutm.domain.model.Appointment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun TanggalScreen(
    viewModel: TanggalViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments
    val selectedDate by viewModel.selectedDate
    val currentMonthStartDate by viewModel.currentMonthStartDate
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    val today = LocalDate.now()
    val currentYearMonth = YearMonth.from(currentMonthStartDate)
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    LaunchedEffect(appointments) {
        Log.d("CalendarScreen", "Appointments loaded: $appointments")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Kalender",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Calendar header with month navigation
        CalendarHeader(
            currentMonthStartDate = currentMonthStartDate,
            onPreviousMonth = { viewModel.navigateToPreviousMonth() },
            onNextMonth = { viewModel.navigateToNextMonth() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Days of week header
        DaysOfWeekHeader()

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.height(320.dp)
        ) {
            // Empty cells before the first day of month
            items(startDayOfWeek) {
                CalendarDay(
                    day = null,
                    isSelected = false,
                    isToday = false,
                    isDisabled = true,
                    hasAppointment = false,
                    onClick = {}
                )
            }

            // Days of the current month
            items(daysInMonth) { day ->
                val date = LocalDate.of(currentYearMonth.year, currentYearMonth.month, day + 1)
                val isSelected = selectedDate == date
                val isToday = date.equals(today)
                val isDisabled = date.isBefore(today)
                val hasAppointment = appointments.any {
                    it.tanggal?.let { appointmentDate ->
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        LocalDate.parse(appointmentDate, formatter).equals(date)
                    } ?: false
                }

                CalendarDay(
                    day = day + 1,
                    isSelected = isSelected,
                    isToday = isToday,
                    isDisabled = isDisabled,
                    hasAppointment = hasAppointment,
                    onClick = {
                        if (!isDisabled) {
                            viewModel.selectDate(date)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appointments section with scroll
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "Janji Temu ${selectedDate?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) ?: ""}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredAppointments = appointments.filter { appointment ->
                    appointment.tanggal?.let { dateStr ->
                        try {
                            val appointmentDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            selectedDate == appointmentDate
                        } catch (e: Exception) {
                            false
                        }
                    } ?: false
                }

                if (filteredAppointments.isEmpty()) {
                    Text(
                        text = "Tidak ada janji temu pada tanggal ini",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredAppointments) { appointment ->
                            AppointmentCard(appointment = appointment)
                        }
                    }
                }
            }
        }

        // Error message if any
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonthStartDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val isPreviousDisabled = currentMonthStartDate.isEqual(LocalDate.now().withDayOfMonth(1))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            enabled = !isPreviousDisabled
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                tint = if (isPreviousDisabled) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = currentMonthStartDate.format(monthYearFormatter),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Starting with Sunday (day 7) then Monday (day 1) to Saturday (day 6)
        val daysOfWeek = listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )

        daysOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CalendarDay(
    day: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    hasAppointment: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = day != null && !isDisabled) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 16.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                if (hasAppointment) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.secondary
                            )
                    )
                }
            }
        }
    }
}
