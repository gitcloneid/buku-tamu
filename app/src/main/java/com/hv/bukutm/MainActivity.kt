package com.hv.bukutm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.presentation.navigation.NavGraph
import com.hv.bukutm.ui.theme.BukuTMTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.material3.rememberDatePickerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hv.bukutm.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BukuTMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = hiltViewModel()
                    val coroutineScope = rememberCoroutineScope()
                    // Only trigger refresh once on app start, with a flag to prevent re-execution
                    var isRefreshDone by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        if (!isRefreshDone) {
                            viewModel.tokenManager.refreshToken.collectLatest { refreshToken ->
                                if (refreshToken != null && !isRefreshDone) {
                                    Log.d("MainActivity", "Refreshing token with: $refreshToken")
                                    viewModel.refreshToken(refreshToken)
                                    isRefreshDone = true // Prevent further refreshes
                                } else {
                                    Log.d("MainActivity", "No refresh token or already refreshed")
                                }
                            }
                        }
                    }

                    NavGraph(
                        navController = navController,
                        tokenManager = tokenManager, // Pass tokenManager to NavGraph
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun refreshToken(refreshToken: String) {
        viewModelScope.launch {
            authRepository.refreshToken(refreshToken).fold(
                onSuccess = { (accessToken, newRefreshToken) ->
                    Log.d("MainViewModel", "Token Refreshed - Access: $accessToken, Refresh: $newRefreshToken")
                    tokenManager.saveTokens(accessToken, newRefreshToken)
                },
                onFailure = { error ->
                    Log.e("MainViewModel", "Refresh Failed: ${error.message}")
                    tokenManager.clearTokens()
                }
            )
        }
    }
}