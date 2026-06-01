package com.zaitsev.liberink.models

data class LoreItem(
    val id: String = "",
    val userId: String = "",
    val worldName: String = "",
    val type: String = "", // "Locations", "Character" і т.д.
    val name: String = "",
    val bio: String = "",
    val extraData: String = "", // Наприклад "Kingdom" або "May 2026"
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)