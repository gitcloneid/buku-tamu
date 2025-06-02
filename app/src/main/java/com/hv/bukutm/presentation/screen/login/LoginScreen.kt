package com.hv.bukutm.presentation.screen.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hv.bukutm.R
import com.hv.bukutm.ui.theme.BukuTMTheme

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.user) {
        when (state.user?.role) {
            "Admin" -> navController.navigate("admin_home") {
                popUpTo("login") { inclusive = true }
            }
            "Guru" -> navController.navigate("teacher_home") {
                popUpTo("login") { inclusive = true }
            }
            "PenerimaTamu" -> navController.navigate("home_penerima_tamu") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LoginScreenContent(
        state = state,
        onLoginClick = { email, password ->
            viewModel.login(email, password)
        }
    )
}

@Composable
fun LoginScreenContent(
    state: LoginUiState,
    onLoginClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }


    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_1_),
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenWidth.times(0.08f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Login Logo",
                    modifier = Modifier.size(screenWidth.times(0.4f))
                )
            }

            //form top padding
            val topPadding = 120.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topPadding),
                horizontalAlignment = Alignment.Start
            ) {
                // Login Buku Tamu text
                Text(
                    text = "Masuk Buku Tamu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2196F3),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Email field label
                Text(
                    text = "Email",
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            "Masukkan Email",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSystemInDarkTheme()) Color(0xFF2B2B2B) else Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF616161) else Color.LightGray,
                        focusedBorderColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3),
                        focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        unfocusedTextColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f),
                        focusedContainerColor = if (isSystemInDarkTheme()) Color(0xFF2B2B2B) else Color.White,
                        unfocusedContainerColor = if (isSystemInDarkTheme()) Color(0xFF2B2B2B) else Color.White,
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field label
                Text(
                    text = "Kata Sandi",
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Masukkan Kata Sandi") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        val icon = if (isPasswordVisible) {
                            painterResource(id = R.drawable.eye_open_svgrepo_com) // Ganti dengan ikon mata terbuka
                        } else {
                            painterResource(id = R.drawable.eye_closed_svgrepo_com) // Ganti dengan ikon mata tertutup
                        }
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = icon,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF616161) else Color.LightGray,
                        focusedBorderColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3),
                        focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        unfocusedTextColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f),
                        focusedContainerColor = if (isSystemInDarkTheme()) Color(0xFF2B2B2B) else Color.White,
                        unfocusedContainerColor = if (isSystemInDarkTheme()) Color(0xFF2B2B2B) else Color.White,
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    ),
                    singleLine = true
                )

                // Forgot password link
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF2196F3),
                                    fontSize = 14.sp,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) { append("Lupa Sandi?") }
                        },
                        onClick = {}
                    )
                }
            }


            val buttonBottomPadding = 90.dp
            val buttonVerticalOffset = 0.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = buttonBottomPadding)
                ) {
                    // Login button
                    OutlinedButton(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier
                            .height(50.dp)
                            .width(110.dp)
                            .offset(y = buttonVerticalOffset),
                        enabled = !state.isLoading,
                        border = BorderStroke(1.5.dp, Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Masuk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                // Error message (if exists)
                if (state.error != null) {
                    Text(
                        text = state.error ?: "Unknown error",
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-35).dp)
                            .offset(x = 15.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun LoginScreenPreview() {
//    BukuTMTheme {
//        LoginScreenContent(
//            state = LoginUiState(),
//            onLoginClick = { _, _ -> }
//        )
//    }
//}