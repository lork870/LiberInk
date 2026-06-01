package com.zaitsev.liberink.models

import com.google.firebase.firestore.Exclude

data class WorldItem(
    @get:Exclude val id: String = "",
    val userId: String = "",
    val name: String = "",
    val timestamp: Long = System.currentTimeMillis()
)