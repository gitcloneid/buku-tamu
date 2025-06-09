package com.hv.bukutm.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.domain.model.Tamu
import com.hv.bukutm.presentation.navigation.TamuNavBarItem
import com.hv.bukutm.presentation.navigation.anjay
import com.hv.bukutm.presentation.screen.profile.TamuProfileScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.presentation.screen.dashboard.tamu.TamuHistoryScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun HomeTamu(
    navController: NavController,
    tokenManager: TokenManager,
    viewModel: HomeTamuViewModel = hiltViewModel()
) {
    val tamuState by viewModel.tamu.collectAsState()
    val nestedNavController = rememberNavController()

    tamuState?.let { tamu ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Content area - takes remaining space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                NavHost(
                    navController = nestedNavController,
                    startDestination = anjay[0].route
                ) {
                    composable(anjay[0].route) { // Dashboard Section
                        TamuDashboard(navController = nestedNavController)
                    }

                    composable(anjay[1].route) {
                        TamuHistoryScreen()
                    }

                    composable(anjay[2].route) { // Profile Section
                        TamuProfileScreen(
                            tokenManager = tokenManager,
                            tamu = tamu,
                            onLogout = {
                                viewModel.logout {
                                    navController.navigate("tamu_login") {
                                        popUpTo("home_tamu") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Bottom navigation bar
            TamuNavBarItem(
                navController = nestedNavController,
                items = anjay
            )
        }
    } ?: run {
        // Show loading state if tamu is null
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@HiltViewModel
class HomeTamuViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _tamu = MutableStateFlow<Tamu?>(null)
    val tamu: StateFlow<Tamu?> = _tamu.asStateFlow()

    init {
        loadTamu()
    }

    private fun loadTamu() {
        viewModelScope.launch {
            tokenManager.tamu.collectLatest { tamu ->
                _tamu.value = tamu
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearTokens()
            onLogout()
        }
    }
}