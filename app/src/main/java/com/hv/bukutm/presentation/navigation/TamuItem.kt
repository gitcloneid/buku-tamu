package com.hv.bukutm.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.hv.bukutm.R

sealed class TamuItem(
    val route: String,
    @DrawableRes val icon: Int
) {
    object Dashboard_Tamu : TamuItem("dashboard_section",  R.drawable.home_alt_1)
    object Qr_Tamu : TamuItem("tanggal_section",  R.drawable.qr_scan_svgrepo_com)
    object Profile_Tamu : TamuItem("profile_section", R.drawable.user_svgrepo_com)
}

val TamuBarItem = listOf(
    TamuItem.Dashboard_Tamu,
    TamuItem.Qr_Tamu,
    TamuItem.Profile_Tamu,
)