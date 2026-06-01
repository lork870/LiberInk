package com.zaitsev.liberink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zaitsev.liberink.models.LoreItem
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLoreItemScreen(navController: NavController, categoryType: String, worldName: String? = "The Valley of Seven Thunders") {
    val theme = LiberInkTheme.colors
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val currentWorldName = worldName?.replace("%20", " ") ?: "Untitled World"

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var extraData by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // ЛОГІКА ЗБЕРЕЖЕННЯ ТІЛЬКИ ТЕКСТУ В FIRESTORE
    fun saveLoreItem() {
        if (name.isBlank() || currentUser == null) {
            Toast.makeText(context, "Full Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        val loreItem = LoreItem(
            userId = currentUser.uid,
            worldName = currentWorldName,
            type = categoryType,
            name = name,
            bio = bio,
            extraData = extraData,
            imageUrl = "",
            timestamp = System.currentTimeMillis()
        )

        db.collection("lore_items").add(loreItem)
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(context, "$categoryType Created!", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Повертаємось на попередній екран
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Scaffold(containerColor = theme.paperMain) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Create a $categoryType", style = MaterialTheme.typography.displayLarge, fontSize = 32.sp, color = theme.mainInk)
            Spacer(modifier = Modifier.height(8.dp))
            Text("World: $currentWorldName", fontSize = 14.sp, color = theme.secondaryInk)
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(theme.paperElevated),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    // КНОПКА ФОТО (Тимчасово вимкнена логіка)
                    Card(
                        shape = RoundedCornerShape(percent = if (categoryType == "Character") 50 else 16),
                        colors = CardDefaults.cardColors(Color(0xFFE6BEB3)),
                        modifier = Modifier.size(100.dp).clickable {
                            Toast.makeText(context, "Photo upload is temporarily disabled.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (categoryType == "Character") Icons.Default.Person else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = theme.mainInk,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.mainInk,
                            unfocusedBorderColor = theme.dividerStrong
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Biography (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.mainInk,
                            unfocusedBorderColor = theme.dividerStrong
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { if (!isLoading) expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = extraData,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (categoryType == "Character") "Birth Date" else "Type *") },
                            leadingIcon = { Icon(if (categoryType == "Character") Icons.Default.CalendarToday else Icons.Default.LocationOn, null, tint = theme.mainInk) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.mainInk,
                                unfocusedBorderColor = theme.dividerStrong
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(theme.paperElevated)
                        ) {
                            val options = if (categoryType == "Character") listOf("May 2026", "Unknown") else listOf("City", "Kingdom", "Landmark")
                            options.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = { extraData = option; expandedDropdown = false })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { saveLoreItem() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF4A0E0E)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Create $categoryType", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.clickable { if (!isLoading) navController.popBackStack() }, verticalAlignment = Alignment.CenterVertically) {
                Text("Back", color = theme.mainInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}