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
import com.hv.bukutm.presentation.screen.dashboard.admin.AdminReport
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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Content area - takes remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            NavHost(
                navController = nestedNavController,
                startDestination = AdminHomeBarItem[0].route
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

        // Bottom navigation bar
        AdminBottomNav(
            navController = nestedNavController,
            items = AdminHomeBarItem
        )
    }
}