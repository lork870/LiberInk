package com.zaitsev.liberink.ui.components // Перевір, щоб цей шлях збігався з твоєю структурою папок

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zaitsev.liberink.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF4A0404).copy(alpha = 0.1f),
        windowInsets = WindowInsets(0, 0, 0, 0), // Це прибере відступ від системної смужки знизу
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
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4A142C),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFF4A142C).copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class NavigationItem(val route: String, val icon: Int, val label: String)