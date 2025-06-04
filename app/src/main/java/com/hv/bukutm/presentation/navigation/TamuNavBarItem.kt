package com.hv.bukutm.presentation.navigation

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun TamuNavBarItem(
    navController: NavController,
    items: List<Login_Tamu>,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .height(65.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(
                                when (item) {
                                    Login_Tamu.Dashboard_Tamu -> 28.dp
                                    Login_Tamu.Profile_Tamu -> 28.dp   // Larger to compensate for smaller intrinsic size
                                    else -> 28.dp
                                }
                            )
                            .padding(2.dp)
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    Log.d("BottomNavBar", "Navigating to: ${item.route}")
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFA640),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = Color.Transparent
                ),
                interactionSource = remember { MutableInteractionSource() }
            )
        }
    }
}