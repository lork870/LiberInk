package com.zaitsev.liberink.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zaitsev.liberink.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.navigation.NavOptionsBuilder
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (String, NavOptionsBuilder.() -> Unit) -> Unit
) {
    val theme = LiberInkTheme.colors

    NavigationBar(
        containerColor = theme.paperElevated,
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.height(80.dp),
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            NavigationItem("home", R.drawable.ic_home, "Home"),
            NavigationItem("lore", R.drawable.ic_lore, "Lore"),
            NavigationItem("notes", R.drawable.ic_notes, "Notes"),
            NavigationItem("profile", R.drawable.ic_profile, "Profile")
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    onNavigate(item.route) {
                        popUpTo("home") {
                            saveState = true
                        }
                        launchSingleTop = true

                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (currentRoute == item.route) theme.mainInk else theme.secondaryInk
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = theme.mainInk,
                    unselectedIconColor = theme.secondaryInk,
                    indicatorColor = theme.accentGold.copy(alpha = 0.2f)
                )
            )
        }
    }
}

data class NavigationItem(val route: String, val icon: Int, val label: String)