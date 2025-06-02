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
import com.hv.bukutm.presentation.navigation.BottomNavBar
import com.hv.bukutm.presentation.navigation.HomeNavBarItems
import com.hv.bukutm.presentation.screen.dashboard.TanggalScreen
import com.hv.bukutm.presentation.screen.dashboard.DashboardGuruScreen
import com.hv.bukutm.presentation.screen.notification.NotificationScreen
import com.hv.bukutm.presentation.screen.profile.ProfileScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeTeacher(
    navController: NavController,
    modifier: Modifier = Modifier,
    tokenManager: TokenManager
) {
    val nestedNavController = rememberNavController()
    val viewModel: HomeViewModel = hiltViewModel()

    var accessToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.tokenManager.accessToken.collectLatest { token ->
            Log.d("HomeTeacher", "Access Token Updated: $token")
            accessToken = token
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = nestedNavController,
                items = HomeNavBarItems
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = HomeNavBarItems[0].route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeNavBarItems[0].route) { // Dashboard Section
                DashboardGuruScreen(navController = navController)
            }
            composable(HomeNavBarItems[1].route) { // Tanggal Section
                TanggalScreen()
            }
            composable(HomeNavBarItems[2].route) { // Notifikasi Section
                NotificationScreen()
            }
            composable(HomeNavBarItems[3].route) { // Profile Section
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