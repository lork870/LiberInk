package com.zaitsev.liberink.ui

import com.zaitsev.liberink.R

// Тільки цей клас залишаємо тут
sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Home : Screen("home", "Home", R.drawable.ic_home)
    object Lore : Screen("lore", "Lore", R.drawable.ic_lore)
    object Notes : Screen("notes", "Notes", R.drawable.ic_notes)
    object Profile : Screen("profile", "Profile", R.drawable.ic_profile)
}