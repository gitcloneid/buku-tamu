package com.hv.bukutm.presentation.screen.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun TamuLoginScreen(
    navController: NavController,
    viewModel: TamuLoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.tamu) {
        if (state.tamu != null) {
            navController.navigate("home_tamu") {
                popUpTo("tamu_login") { inclusive = true }
            }
        }
    }

    TamuLoginScreenContent(
        state = state,
        onLoginClick = { qrCode ->
            viewModel.loginWithQrCode(qrCode)
        },
        navController = navController
    )
}

@Composable
fun TamuLoginScreenContent(
    state: TamuLoginUiState,
    onLoginClick: (String) -> Unit,
    navController: NavController
) {
    var qrCode by remember { mutableStateOf("") }
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
                    modifier = Modifier.size(screenWidth.times(0.3f))
                )


                Text(
                    text = "SMKN 2 SINGOSARI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF7F5F5),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 185.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Masuk Sebagai Tamu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2196F3),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Kode",
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                OutlinedTextField(
                    value = qrCode,
                    onValueChange = { qrCode = it },
                    placeholder = {
                        Text(
                            "Masukkan Kode (Dikirim Dari Whatsapp)",
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Spacer(modifier = Modifier.padding(12.dp))
                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF2196F3),
                                    fontSize = 14.sp,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) { append("Masuk Sebagai Pengguna") }
                        },
                        onClick = {
                            navController.navigate("login")
                        }
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
                    OutlinedButton(
                        onClick = { onLoginClick(qrCode) },
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TamuLoginScreenPreview() {
    BukuTMTheme {
        TamuLoginScreenContent(
            state = TamuLoginUiState(),
            onLoginClick = { _ -> },
            navController = rememberNavController()
        )
    }
}