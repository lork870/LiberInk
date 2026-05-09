package com.zaitsev.liberink.models

data class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)