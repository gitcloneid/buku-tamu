package com.hv.bukutm.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.hv.bukutm.R

sealed class NavBarItem(
    val route: String,
    @DrawableRes val icon: Int
) {
    object Dashboard_Guru : NavBarItem("dashboard_section",  R.drawable.home_alt_1)
    object Tanggal_Guru : NavBarItem("tanggal_section",  R.drawable.notes_1)
    object Notifikasi_Guru : NavBarItem("notifikasi_section",  R.drawable.notification_bell_1397_svgrepo_com)
    object Profile_Guru : NavBarItem("profile_section", R.drawable.user_svgrepo_com)
}

val HomeNavBarItems = listOf(
    NavBarItem.Dashboard_Guru,
    NavBarItem.Tanggal_Guru,
    NavBarItem.Notifikasi_Guru,
    NavBarItem.Profile_Guru,
)