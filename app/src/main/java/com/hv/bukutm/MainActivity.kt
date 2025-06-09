package com.hv.bukutm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.data.TokenManager
import com.hv.bukutm.presentation.navigation.NavGraph
import com.hv.bukutm.ui.theme.BukuTMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BukuTMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = hiltViewModel()
                    val authState by viewModel.authState.collectAsStateWithLifecycle()
                    val showNoInternetDialog by viewModel.showNoInternetDialog.collectAsStateWithLifecycle()

                    if (showNoInternetDialog) {
                        AlertDialog(
                            onDismissRequest = { /* Dialog can't be dismissed without retrying */ },
                            title = { Text("Tidak Ada Koneksi Internet") },
                            text = { Text("Tidak ada internet yang tersambung. Silahkan coba lagi.") },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.retryAuthCheck() }
                                ) {
                                    Text("Coba Lagi")
                                }
                            }
                        )
                    }

                    // The main navigation logic for the app.
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(
                            navController = navController,
                            authState = authState,
                            tokenManager = viewModel.publicTokenManager
                        )
                    }
                }
            }
        }
    }
}

/**
 * This composable acts as a router. It observes the authState and
 * determines the correct start destination for the NavGraph.
 */
@Composable
fun AppNavigation(
    navController: androidx.navigation.NavHostController,
    authState: AuthState,
    tokenManager: TokenManager
) {
    // A when statement cleanly handles which UI to show.
    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "logo",
                    modifier = Modifier.size(180.dp)
                )
            }
        }
        is AuthState.Authenticated -> {
            // When authenticated, determine the start destination based on the user's role.
            val startDestination = when (authState.role) {
                "Admin" -> "admin_home"
                "Guru" -> "teacher_home"
                "Penerima Tamu" -> "home_penerima_tamu"
                "Tamu" -> "home_tamu"
                else -> "login" // Fallback for any unknown roles
            }
            NavGraph(
                navController = navController,
                tokenManager = tokenManager,
                startDestination = startDestination
            )
        }
        is AuthState.Unauthenticated -> {
            // If the user is not logged in, start at the login screen.
            NavGraph(
                navController = navController,
                tokenManager = tokenManager,
                startDestination = "login"
            )
        }
    }
}
