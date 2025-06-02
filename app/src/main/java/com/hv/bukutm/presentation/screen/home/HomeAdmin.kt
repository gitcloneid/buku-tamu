package com.hv.bukutm.presentation.screen.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.presentation.navigation.AdminBottomNav
import com.hv.bukutm.presentation.navigation.AdminHomeBarItem
import com.hv.bukutm.presentation.navigation.BottomNavBar
import com.hv.bukutm.presentation.screen.dashboard.AddAppointmentScreen
import com.hv.bukutm.presentation.screen.dashboard.TanggalScreen
import com.hv.bukutm.presentation.screen.dashboard.admin.AdminReport
import com.hv.bukutm.presentation.screen.dashboard.admin.MonthlyReportScreen
import com.hv.bukutm.presentation.screen.dashboard.penerimatamu.ReportsScreen
import com.hv.bukutm.presentation.screen.profile.ProfileScreen
import com.hv.bukutm.ui.screen.UserManagementScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeAdmin(
    navController: NavController,
    modifier: Modifier = Modifier,
    tokenManager: TokenManager
) {
    val nestedNavController = rememberNavController()
    val viewModel: HomeViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            AdminBottomNav(
                navController = nestedNavController,
                items = AdminHomeBarItem
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = AdminHomeBarItem[0].route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminHomeBarItem[0].route) { // Dashboard Section
                AdminReport(navController = navController)
            }
            composable(AdminHomeBarItem[1].route) { // Add Appointment Section
                UserManagementScreen()
            }
            composable(AdminHomeBarItem[2].route) { // Profile Section
                ProfileScreen(
                    tokenManager = tokenManager,
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("admin_home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}