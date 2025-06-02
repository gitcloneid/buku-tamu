package com.hv.bukutm.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.hv.bukutm.R

sealed class AdminBarItem(
    val route: String,
    @DrawableRes val icon: Int
) {
    object Dashboard_Admin : AdminBarItem("dashboard_section",  R.drawable.home_alt_1)
    object Add_Admin : AdminBarItem("add_section", R.drawable.add_plus_svgrepo_com)
    object Profile_Admin : AdminBarItem("profile_section", R.drawable.user_svgrepo_com)
}

val AdminHomeBarItem = listOf(
    AdminBarItem.Dashboard_Admin,
    AdminBarItem.Add_Admin,
    AdminBarItem.Profile_Admin
)