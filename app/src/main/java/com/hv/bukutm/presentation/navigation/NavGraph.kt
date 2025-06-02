package com.hv.bukutm.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.presentation.screen.dashboard.AddAppointmentScreen
import com.hv.bukutm.presentation.screen.dashboard.HistoryScreen
import com.hv.bukutm.presentation.screen.dashboard.admin.MonthlyReportScreen
import com.hv.bukutm.presentation.screen.home.HomeAdmin
import com.hv.bukutm.presentation.screen.home.HomePenerimaTamu
import com.hv.bukutm.presentation.screen.home.HomeTeacher
import com.hv.bukutm.presentation.screen.login.LoginScreen
import com.hv.bukutm.presentation.screen.notification.NotificationScreen
import com.hv.bukutm.utils.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@Composable
fun NavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    modifier: Modifier = Modifier
) {
    val viewModel: NavGraphViewModel = hiltViewModel()
    val startDestination = remember { mutableStateOf("login") }

    LaunchedEffect(Unit) {
        val token = viewModel.tokenManager.accessToken.firstOrNull()
        if (token != null) {
            val role = JwtUtils.getRoleFromToken(token)
            Log.d("LoginRole", "User role: $role") // Logging the role
            startDestination.value = when (role) {
                "Admin" -> "admin_home"
                "Guru" -> "teacher_home"
                "PenerimaTamu" -> "home_penerima_tamu"
                else -> "login"
            }
        } else {
            Log.d("LoginRole", "No token found, redirecting to login")
            startDestination.value = "login"
        }
    }



    NavHost(
        navController = navController,
        startDestination = startDestination.value,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("admin_home") {
            HomeAdmin(navController = navController, tokenManager = tokenManager)
        }
        composable("teacher_home") {
            HomeTeacher(navController = navController, tokenManager = tokenManager)
        }
        composable("home_penerima_tamu") {
            HomePenerimaTamu(navController = navController, tokenManager = tokenManager)
        }
        composable("add_appointment") {
            AddAppointmentScreen(navController = navController)
        }
        composable("history") {
            HistoryScreen(navController = navController)
        }
        composable("notification") {
            NotificationScreen()
        }
        composable("monthly_reports") {
            MonthlyReportScreen(navController = navController)
        }
    }
}

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    val tokenManager: TokenManager
) : ViewModel()