package com.hv.bukutm.data

import com.hv.bukutm.data.api.DailyReport
import com.hv.bukutm.data.api.MonthlyReport
import com.hv.bukutm.data.api.ReportsApi
import com.hv.bukutm.domain.repository.ReportsRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val api: ReportsApi
) : ReportsRepository {

    override suspend fun getDailyReport(token: String): Result<DailyReport> {
        return try {
            val report = api.getDailyReport("Bearer $token")
            Result.success(report)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMonthlyReport(token: String, month: Int?): Result<MonthlyReport> {
        return try {
            val report = api.getMonthlyReport("Bearer $token", month)
            Result.success(report)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()}: ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}