package com.hv.bukutm.domain.repository

import com.hv.bukutm.data.api.DailyReport
import com.hv.bukutm.data.api.MonthlyReport

interface ReportsRepository {
    suspend fun getDailyReport(token: String): Result<DailyReport>
    suspend fun getMonthlyReport(token: String, month: Int? = null): Result<MonthlyReport>
}