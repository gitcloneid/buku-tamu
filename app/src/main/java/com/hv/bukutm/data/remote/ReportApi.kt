package com.hv.bukutm.data.api

import com.hv.bukutm.domain.model.CreateUserRequest
import com.hv.bukutm.domain.model.UpdateUserRequest
import com.hv.bukutm.domain.model.User
import com.hv.bukutm.domain.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportsApi {

    @GET("api/reports/daily")
    suspend fun getDailyReport(
        @Header("Authorization") token: String,
        @Header("Accept") accept: String = "text/plain"
    ): DailyReport

    @GET("api/reports/monthly")
    suspend fun getMonthlyReport(
        @Header("Authorization") token: String,
        @Query("month") month: Int? = null,
        @Header("Accept") accept: String = "text/plain"
    ): MonthlyReport
}

data class DailyReport(
    val date: String,
    val totalAppointments: Int,
    val completed: Int,
    val waiting: Int,
    val late: Int,
    val completionRate: Float,
    val byTeacher: List<TeacherReport>
)

data class TeacherReport(
    val idGuru: Int,
    val nama: String,
    val total: Int,
    val completed: Int
)

data class MonthlyReport(
    val month: Int,
    val year: Int,
    val totalAppointments: Int,
    val completionRate: Float,
    val weeklyStats: List<WeeklyStats>
)

data class WeeklyStats(
    val weekNumber: Int,
    val total: Int,
    val completed: Int
)