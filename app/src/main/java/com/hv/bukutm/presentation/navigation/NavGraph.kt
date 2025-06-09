package com.hv.bukutm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.presentation.screen.dashboard.AddAppointmentScreen
import com.hv.bukutm.presentation.screen.dashboard.HistoryScreen
import com.hv.bukutm.presentation.screen.dashboard.admin.MonthlyReportScreen
import com.hv.bukutm.presentation.screen.home.HomeAdmin
import com.hv.bukutm.presentation.screen.home.HomePenerimaTamu
import com.hv.bukutm.presentation.screen.home.HomeTamu
import com.hv.bukutm.presentation.screen.home.HomeTeacher
import com.hv.bukutm.presentation.screen.login.LoginScreen
import com.hv.bukutm.presentation.screen.login.TamuLoginScreen
import com.hv.bukutm.presentation.screen.notification.NotificationScreen

/**
 * Defines the navigation graph for the application.
 * This composable is now much simpler, as it only needs to know the start destination.
 *
 * @param navController The navigation controller.
 * @param tokenManager The token manager, passed down to screens that need it.
 * @param startDestination The route to show when the graph is first composed.
 * @param modifier A modifier for the NavHost.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    startDestination: String, // The start destination is now passed in as a parameter.
    modifier: Modifier = Modifier
) {
    // The NavGraphViewModel and LaunchedEffect are no longer needed here.
    NavHost(
        navController = navController,
        startDestination = startDestination, // Use the provided start destination.
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("tamu_login") {
            TamuLoginScreen(navController = navController)
        }
        composable("home_tamu") {
            HomeTamu(navController = navController, tokenManager = tokenManager)
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
