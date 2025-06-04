package com.hv.bukutm.domain.repository

import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.domain.model.Tamu

interface AppointmentRepository {
    suspend fun getTodayAppointments(token: String): Result<List<Appointment>>
    suspend fun createTamu(token: String, nama: String, telepon: String): Result<Tamu>
    suspend fun createAppointment(
        token: String,
        idTamu: Int,
        tanggal: String,
        waktu: String,
        keperluan: String
    ): Result<String>
    suspend fun getCompletedAppointments(
        token: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<List<Appointment>>
    suspend fun getPendingAppointments(
        token: String,
        page: Int = 1,
        limit: Int = 1000000000
    ): Result<List<Appointment>>
    suspend fun getAppointmentByQr(token: String, kodeQr: String): Result<Appointment>
    suspend fun getTamuByQr(token: String? = null, kodeQr: String): Result<Appointment>
    suspend fun updateAppointmentStatus(token: String, id: Int, status: String): Result<Appointment>
    suspend fun rescheduleAppointment(
        token: String,
        id: Int,
        tanggal: String,
        waktu: String
    ): Result<Appointment>
}