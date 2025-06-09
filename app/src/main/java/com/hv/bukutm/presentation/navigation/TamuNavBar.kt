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
fun TamuNavBar(
    navController: NavController,
    items: List<TamuItem>,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .height(80.dp),
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
                                    TamuItem.Dashboard_Tamu -> 24.dp
                                    TamuItem.Qr_Tamu -> 24.dp
                                    TamuItem.Profile_Tamu -> 24.dp
                                    else -> 24.dp
                                }
                            )
                            .padding(1.dp)
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
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
                interactionSource = remember { MutableInteractionSource() } // Provide an empty interaction source
            )
        }
    }
}