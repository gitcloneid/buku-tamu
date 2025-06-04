package com.hv.bukutm.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.hv.bukutm.R

sealed class Login_Tamu(
    val route: String,
    @DrawableRes val icon: Int
) {
    object Dashboard_Tamu : Login_Tamu("dashboard_section",  R.drawable.home_alt_1)
    object Profile_Tamu : Login_Tamu("profile_section", R.drawable.user_svgrepo_com)
}

val anjay = listOf(
    Login_Tamu.Dashboard_Tamu,
    Login_Tamu.Profile_Tamu,
)