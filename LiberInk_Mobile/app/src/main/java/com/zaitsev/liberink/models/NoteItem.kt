package com.zaitsev.liberink.models

import androidx.compose.ui.graphics.Color
import com.zaitsev.liberink.models.NoteItem

/**
 * Модель даних для нотатки в LiberInk.
 * Використовується для відображення в сітці та збереження у Firebase.
 */
data class NoteItem(
    val id: String = "",           // Унікальний ID документа у Firestore
    val userId: String = "",       // ID користувача (для безпеки та синхронізації)
    val title: String = "",        // Заголовок нотатки
    val content: String = "",      // Основний текст
    val type: String = "text",     // Тип: "text", "quote", або "sketch"
    val timestamp: Long = System.currentTimeMillis() // Час створення/редагування
)

/**
 * Допоміжна модель для ескізів (якщо вони мають специфічні властивості)
 */
data class SketchInfo(
    val imageUrl: String = "",
    val localResId: Int? = null
)