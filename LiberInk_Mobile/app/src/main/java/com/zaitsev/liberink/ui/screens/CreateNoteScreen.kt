package com.zaitsev.liberink.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    navController: NavController,
    noteId: String? = null,
    initialTitle: String = "",
    initialContent: String = ""
) {
    val theme = LiberInkTheme.colors

    var isEditing by remember { mutableStateOf(noteId == null) }
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }

    // ПРАВИЛЬНЕ ОГОЛОШЕННЯ СТАНУ
    var isSaving by remember { mutableStateOf(false) }

    // Логіка збереження
    val finalSaveAction = {
        if (!isSaving) {
            if (title.isNotBlank() || content.isNotBlank()) {
                isSaving = true
                Log.d("LiberInk", "Початок збереження...")

                saveNoteToFirebase(noteId, title, content) { success ->
                    if (success) {
                        Log.d("LiberInk", "Збережено успішно, виходимо")
                        navController.popBackStack()
                    } else {
                        Log.e("LiberInk", "Помилка збереження")
                        isSaving = false
                    }
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    // Обробка кнопки назад
    BackHandler(enabled = !isSaving) {
        if (isEditing && noteId != null) {
            isEditing = false
        } else {
            finalSaveAction()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F3E9),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { finalSaveAction() }, enabled = !isSaving) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = theme.mainInk
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.mainInk
                        )
                    }
                }

                Row {
                    if (!isEditing && !isSaving) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = theme.mainInk)
                        }
                    }

                    IconButton(onClick = { /* Опції */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = theme.mainInk)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isEditing) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = theme.mainInk),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = theme.mainInk
                    ),
                    enabled = !isSaving
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Note", fontSize = 18.sp, color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 18.sp, color = theme.mainInk),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = theme.mainInk
                    ),
                    enabled = !isSaving
                )
            } else {
                Text(
                    text = if (title.isBlank()) "Untitled" else title,
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = theme.mainInk),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Text(
                    text = content,
                    style = TextStyle(fontSize = 18.sp, color = theme.mainInk, lineHeight = 28.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Функція збереження винесена за межі Composable
 */
fun saveNoteToFirebase(
    noteId: String?,
    title: String,
    content: String,
    onComplete: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    val noteData = hashMapOf(
        "userId" to user.uid,
        "title" to title,
        "content" to content,
        "type" to "text",
        "timestamp" to System.currentTimeMillis()
    )

    if (noteId == null) {
        val newDocRef = db.collection("notes").document()
        noteData["id"] = newDocRef.id
        newDocRef.set(noteData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    } else {
        noteData["id"] = noteId
        db.collection("notes").document(noteId).set(noteData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}