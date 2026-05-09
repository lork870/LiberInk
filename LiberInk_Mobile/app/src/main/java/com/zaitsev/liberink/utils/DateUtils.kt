package com.zaitsev.liberink.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String {
    if (this <= 0) return "N/A"

    val date = Date(this)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    return formatter.format(date)
}