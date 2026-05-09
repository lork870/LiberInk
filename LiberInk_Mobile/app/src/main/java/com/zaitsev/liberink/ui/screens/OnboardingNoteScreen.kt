package com.zaitsev.liberink.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zaitsev.liberink.R
import com.zaitsev.liberink.ui.components.AppHeader
import com.zaitsev.liberink.ui.theme.DarkWine20
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import android.graphics.Color as AndroidColor
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun OnboardingNoteScreen(navController: NavController) {
    val theme = LiberInkTheme.colors

    var isLoading by remember { mutableStateOf(false) }

    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    var noteTitle by rememberSaveable { mutableStateOf("") }
    var noteDescription by rememberSaveable { mutableStateOf("") }

    // Стани полів для другого кроку
    var penName by rememberSaveable { mutableStateOf("") }
    var pseudonymDescription by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.paperMain),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader()

        // 1. Заголовки
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Let's Create",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = theme.mainInk
            )
            Text(
                text = "Your First Note",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = theme.mainInk
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Картка Степера
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .drawBehind {
                    val shadowColor = theme.shadow
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = AndroidColor.TRANSPARENT
                            setShadowLayer(30.dp.toPx(), 0f, 10.dp.toPx(), shadowColor.toArgb())
                        }
                        drawRoundRect(
                            0f,
                            0f,
                            size.width,
                            size.height,
                            28.dp.toPx(),
                            28.dp.toPx(),
                            paint
                        )
                    }
                }
                .clip(RoundedCornerShape(28.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.paper_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.6f)))

            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            ) {
                // Індикатор кроку та назва секції
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(theme.mainInk, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = currentStep.toString(),
                            color = theme.paperElevated,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (currentStep == 1) "Note Details" else "Pseudonym",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        color = theme.mainInk
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Динамічний контент залежно від кроку
                if (currentStep == 1) {
                    // КРОК 1
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text(text = "Note Title *") },

                        placeholder = {
                            Text(
                                text = "Enter your note title",
                                color = DarkWine20
                            )
                        },

                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.mainInk,
                            unfocusedBorderColor = theme.dividerStrong
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pseudonymDescription,
                        onValueChange = { pseudonymDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = {
                            Text(
                                text = "A brief description or annotation of your note",
                                color = DarkWine20
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.mainInk,
                            unfocusedBorderColor = theme.dividerStrong
                        )
                    )
                } else {
                    // КРОК 2
                    OutlinedTextField(
                        value = penName,
                        onValueChange = { penName = it },
                        label = { Text(text = "Pen Name *") },

                        placeholder = {
                            Text(
                                text = "Enter your pseudonym",
                                color = DarkWine20
                            )
                        },

                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.mainInk,
                            unfocusedBorderColor = theme.dividerStrong
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = noteDescription,
                        onValueChange = { noteDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = {
                            Text(
                                text = "A brief description of your pseudonym of the author",
                                color = DarkWine20
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.mainInk,
                            unfocusedBorderColor = theme.dividerStrong
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка Continue / Complete
                Button(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()

                        if (currentStep == 1) {
                            // КРОК 1: Тільки валідація та перехід
                            if (noteTitle.isNotBlank()) {
                                currentStep = 2
                            }
                        } else {
                            // КРОК 2: Зберігаємо все разом
                            if (penName.isNotBlank()) {
                                isLoading = true

                                // Збираємо дані з усіх полів обох кроків
                                val fullNoteData = hashMapOf(
                                    "title" to noteTitle,
                                    "description" to noteDescription,
                                    "author_pen_name" to penName,
                                    "author_bio" to pseudonymDescription,
                                    "createdAt" to System.currentTimeMillis(),
                                    "isTemporary" to true // Помітка, що запис ще не прив'язаний до акаунта
                                )

                                db.collection("onboarding_notes")
                                    .add(fullNoteData)
                                    .addOnSuccessListener { documentReference ->

                                        navController.navigate("registration?noteId=${documentReference.id}")
                                        isLoading = false
                                        println("Успішно збережено з ID: ${documentReference.id}")

                                        // Переходимо на екран реєстрації
                                        // Можна передати ID документа, щоб потім прив'язати його до юзера
                                        navController.navigate("registration?noteId=${documentReference.id}")
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        println("Помилка запису: $e")
                                    }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.mainInk,
                        disabledContainerColor = theme.mainInk.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .drawBehind {
                            val shadowColor = Color.Black.copy(alpha = 0.4f)
                            val shadowBlur = 15.dp.toPx()
                            val offsetY = 5.dp.toPx()

                            drawContext.canvas.nativeCanvas.apply {
                                val paint = Paint().apply {
                                    color = AndroidColor.TRANSPARENT
                                    setShadowLayer(
                                        shadowBlur,
                                        0f,
                                        offsetY,
                                        shadowColor.toArgb()
                                    )
                                }
                                drawRoundRect(
                                    0f,
                                    0f,
                                    size.width,
                                    size.height,
                                    28.dp.toPx(),
                                    28.dp.toPx(),
                                    paint
                                )
                            }
                        }
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = theme.paperElevated,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentStep == 1) "Next Step" else "Complete & Register",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = theme.paperElevated
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = theme.paperElevated
                            )
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                navController.navigate("authorization") {
                    popUpTo("onboardingNoteScreen") { inclusive = true }
                }
            },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "Skip to library",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.mainInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal            )
        }
    }
}

@Composable
fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    theme: com.zaitsev.liberink.ui.theme.LiberInkColors // Твій тип кольорів
) {
    Column {
        Text(text = label, fontSize = 14.sp, color = theme.mainInk)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = DarkWine20) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.mainInk.copy(alpha = 0.5f),
                unfocusedBorderColor = theme.dividerStrong.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )
    }
}