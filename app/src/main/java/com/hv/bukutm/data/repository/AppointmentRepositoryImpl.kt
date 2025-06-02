package com.hv.bukutm.data.repository

import com.hv.bukutm.data.remote.AppointmentApi
import com.hv.bukutm.data.remote.AppointmentRequest
import com.hv.bukutm.data.remote.TamuRequest
import com.hv.bukutm.domain.model.Appointment
import com.hv.bukutm.domain.model.Tamu
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.repository.AppointmentRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentApi: AppointmentApi
) : AppointmentRepository {

    override suspend fun getTodayAppointments(token: String): Result<List<Appointment>> {
        return try {
            val response = appointmentApi.getTodayAppointments("Bearer $token")
            val appointments = response.map { appointmentResponse ->
                Appointment(
                    idJanjiTemu = appointmentResponse.idJanjiTemu,
                    tanggal = appointmentResponse.tanggal,
                    waktu = appointmentResponse.waktu,
                    status = appointmentResponse.status,
                    keperluan = appointmentResponse.keperluan,
                    kodeQr = appointmentResponse.kodeQr,
                    tamu = Tamu(
                        idTamu = appointmentResponse.tamu.idTamu,
                        nama = appointmentResponse.tamu.nama,
                        telepon = appointmentResponse.tamu.telepon
                    ),
                    guru = User(
                        idPengguna = appointmentResponse.guru.idPengguna,
                        nama = appointmentResponse.guru.nama,
                        email = appointmentResponse.guru.email ?: "",
                        role = appointmentResponse.guru.role ?: "",
                        token = "",
                        refreshToken = ""
                    )
                )
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTamu(token: String, nama: String, telepon: String): Result<Tamu> {
        return try {
            val request = TamuRequest(nama = nama, telepon = telepon)
            val response = appointmentApi.createTamu("Bearer $token", request)
            val tamu = Tamu(
                idTamu = response.idTamu,
                nama = response.nama,
                telepon = response.telepon
            )
            Result.success(tamu)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createAppointment(
        token: String,
        idTamu: Int,
        tanggal: String,
        waktu: String,
        keperluan: String
    ): Result<String> {
        return try {
            val request = AppointmentRequest(
                idTamu = idTamu,
                tanggal = tanggal,
                waktu = waktu,
                keperluan = keperluan
            )
            val response = appointmentApi.createAppointment("Bearer $token", request)
            Result.success(response.kodeQr)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedAppointments(
        token: String,
        page: Int,
        limit: Int
    ): Result<List<Appointment>> {
        return try {
            val response = appointmentApi.getCompletedAppointments("Bearer $token", "Selesai", page, limit)
            val appointments = response.data.map { appointmentResponse ->
                Appointment(
                    idJanjiTemu = appointmentResponse.idJanjiTemu,
                    tanggal = appointmentResponse.tanggal,
                    waktu = appointmentResponse.waktu,
                    status = appointmentResponse.status,
                    keperluan = appointmentResponse.keperluan,
                    kodeQr = appointmentResponse.kodeQr,
                    tamu = Tamu(
                        idTamu = appointmentResponse.tamu.idTamu,
                        nama = appointmentResponse.tamu.nama,
                        telepon = appointmentResponse.tamu.telepon
                    ),
                    guru = User(
                        idPengguna = appointmentResponse.guru.idPengguna,
                        nama = appointmentResponse.guru.nama,
                        email = appointmentResponse.guru.email ?: "",
                        role = appointmentResponse.guru.role ?: "",
                        token = "",
                        refreshToken = ""
                    )
                )
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getPendingAppointments(
        token: String,
        page: Int,
        limit: Int
    ): Result<List<Appointment>> {
        return try {
            val response = appointmentApi.getAppointments("Bearer $token", status = "Menunggu", page, limit)
            val appointments = response.data.map { appointmentResponse ->
                Appointment(
                    idJanjiTemu = appointmentResponse.idJanjiTemu,
                    tanggal = appointmentResponse.tanggal,
                    waktu = appointmentResponse.waktu,
                    status = appointmentResponse.status,
                    keperluan = appointmentResponse.keperluan,
                    kodeQr = appointmentResponse.kodeQr,
                    tamu = Tamu(
                        idTamu = appointmentResponse.tamu.idTamu,
                        nama = appointmentResponse.tamu.nama,
                        telepon = appointmentResponse.tamu.telepon
                    ),
                    guru = User(
                        idPengguna = appointmentResponse.guru.idPengguna,
                        nama = appointmentResponse.guru.nama,
                        email = appointmentResponse.guru.email ?: "",
                        role = appointmentResponse.guru.role ?: "",
                        token = "",
                        refreshToken = ""
                    )
                )
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getAppointmentByQr(token: String, kodeQr: String): Result<Appointment> {
        return try {
            if (kodeQr.isBlank()) {
                return Result.failure(Exception("Invalid QR code"))
            }
            val response = appointmentApi.getAppointmentByQr("Bearer $token", kodeQr)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAppointmentStatus(token: String, id: Int, status: String): Result<Appointment> {
        return try {
            val appointment = appointmentApi.updateAppointmentStatus(
                token = "Bearer $token",
                id = id,
                status = status
            )
            Result.success(appointment)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}