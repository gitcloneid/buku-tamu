package com.hv.bukutm.data.remote

import com.hv.bukutm.domain.model.Appointment
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AppointmentApi {
    @GET("api/appointments/today")
    suspend fun getTodayAppointments(
        @Header("Authorization") authToken: String
    ): List<AppointmentResponse>

    @POST("api/tamu")
    suspend fun createTamu(
        @Header("Authorization") authToken: String,
        @Body tamuRequest: TamuRequest
    ): TamuResponse

    @POST("api/appointments")
    suspend fun createAppointment(
        @Header("Authorization") authToken: String,
        @Body appointmentRequest: AppointmentRequest
    ): AppointmentResponse

    @GET("api/appointments")
    suspend fun getCompletedAppointments(
        @Header("Authorization") authToken: String,
        @Query("status") status: String = "Selesai,Telat",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10000000,
        @Query("lastMonths") lastMonths: Int? = null
    ): CompletedAppointmentsResponse

    @GET("api/appointments")
    suspend fun getAppointments(
        @Header("Authorization") authToken: String,
        @Query("status") status: String = "Menunggu",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 1000000
    ): PendingAppointmentResponse

    @GET("api/appointments/qr/{kodeQr}")
    suspend fun getAppointmentByQr(
        @Header("Authorization") authToken: String,
        @Path("kodeQr") kodeQr: String
    ): Appointment

    @PUT("api/appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String = "text/plain",
        @Header("Content-Type") contentType: String = "application/json",
        @Path("id") id: Int,
        @Body status: String
    ): Appointment

    @GET("api/tamu/qr/{kodeQr}")
    suspend fun getTamuByQr(
        @Path("kodeQr") kodeQr: String
    ): Appointment

    @PUT("api/appointments/{id}/reschedule")
    suspend fun rescheduleAppointment(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: RescheduleAppointmentRequest
    ): Appointment

    @GET("api/tamu/filter")
    suspend fun getTamuHistory(
        @Header("Authorization") authToken: String? = null,
        @Query("status") status: String = "Telat,Selesai",
        @Query("lastmonth") lastmonth: Int? = null,
        @Query("phoneNumber") phoneNumber: String? = null
    ): List<AppointmentResponse>
}

data class RescheduleAppointmentRequest(
    val tanggal: String,
    val waktu: String
)

data class CompletedAppointmentsResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val data: List<AppointmentResponse>
)

data class PendingAppointmentResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val data: List<AppointmentResponse>
)

data class AppointmentResponse(
    val idJanjiTemu: Int,
    val tanggal: String,
    val waktu: String,
    val status: String,
    val keperluan: String,
    val kodeQr: String,
    val tamu: TamuResponse,
    val guru: GuruResponse
)

data class TamuResponse(
    val idTamu: Int,
    val nama: String,
    val telepon: String
)

data class TamuRequest(
    val nama: String,
    val telepon: String
)

data class AppointmentRequest(
    val idTamu: Int,
    val tanggal: String,
    val waktu: String,
    val keperluan: String
)

data class GuruResponse(
    val idPengguna: Int,
    val nama: String,
    val email: String?,
    val role: String?
)