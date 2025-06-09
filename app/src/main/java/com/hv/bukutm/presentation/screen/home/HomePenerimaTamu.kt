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
import com.hv.bukutm.activity.ScanQrScreen
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.presentation.navigation.TamuBarItem
import com.hv.bukutm.presentation.navigation.TamuNavBar
import com.hv.bukutm.presentation.screen.dashboard.penerimatamu.ReportsScreen
import com.hv.bukutm.presentation.screen.profile.ProfileScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomePenerimaTamu(
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
                startDestination = TamuBarItem[0].route
            ) {
                composable(TamuBarItem[0].route) { // Dashboard Section
                    ReportsScreen(navController = navController)
                }
                composable(TamuBarItem[1].route) { // Add Appointment Section
                    ScanQrScreen(navController = navController)
                }
                composable(TamuBarItem[2].route) { // Profile Section
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
        TamuNavBar(
            navController = nestedNavController,
            items = TamuBarItem
        )
    }
}