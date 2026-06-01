package com.zaitsev.liberink.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zaitsev.liberink.R
import com.zaitsev.liberink.models.WorldItem
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoreScreen(navController: NavController) {
    val theme = LiberInkTheme.colors
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current // Для показів Toast повідомлень

    var searchQuery by remember { mutableStateOf("") }
    var expandedWorldId by remember { mutableStateOf<String?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }

    var showCreateWorldDialog by remember { mutableStateOf(false) }
    var newWorldName by remember { mutableStateOf("") }

    // ВИПРАВЛЕННЯ: Використовуємо State замість mutableStateListOf для 100% оновлення UI
    var worlds by remember { mutableStateOf(emptyList<WorldItem>()) }

    DisposableEffect(currentUser) {
        val listener = if (currentUser != null) {
            db.collection("worlds")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Error fetching worlds: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val fetched = snapshot.documents.mapNotNull {
                            it.toObject(WorldItem::class.java)?.copy(id = it.id)
                        }.sortedByDescending { it.timestamp }

                        // Перезаписуємо список, що гарантовано тригерить перемальовку Compose
                        worlds = fetched
                    }
                }
        } else null
        onDispose { listener?.remove() }
    }

    val fabRotation by animateFloatAsState(targetValue = if (isFabExpanded) 225f else 0f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f), label = "fab_rotation")
    val fabSize by animateDpAsState(targetValue = if (isFabExpanded) 44.dp else 56.dp, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f), label = "fab_size")
    val fabCorner by animateDpAsState(targetValue = if (isFabExpanded) 22.dp else 16.dp, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f), label = "fab_corner")
    val fabColor by animateColorAsState(targetValue = if (isFabExpanded) Color(0xFFCC8500) else Color(0xFFF6A000), animationSpec = tween(200), label = "fab_color")

    if (showCreateWorldDialog) {
        AlertDialog(
            onDismissRequest = { showCreateWorldDialog = false },
            containerColor = theme.paperElevated,
            title = { Text("Name your new world", color = theme.mainInk, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newWorldName,
                    onValueChange = { newWorldName = it },
                    placeholder = { Text("e.g. Kingdom of Etherlend", color = theme.secondaryInk) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.mainInk,
                        unfocusedBorderColor = theme.dividerStrong
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWorldName.isNotBlank() && currentUser != null) {
                            val newWorld = WorldItem(
                                name = newWorldName,
                                userId = currentUser.uid,
                                timestamp = System.currentTimeMillis()
                            )

                            // Зберігаємо і чекаємо результату
                            db.collection("worlds").add(newWorld)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Світ створено!", Toast.LENGTH_SHORT).show()
                                    newWorldName = "" // Очищаємо поле
                                    showCreateWorldDialog = false // Закриваємо діалог
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Помилка бази даних", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateWorldDialog = false }) {
                    Text("Cancel", color = theme.secondaryInk)
                }
            }
        )
    }

    // ГОЛОВНИЙ КОНТЕЙНЕР
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. КОНТЕНТ ЕКРАНА
        Scaffold(
            containerColor = theme.paperMain
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Hello, Lola", style = MaterialTheme.typography.displayLarge, fontSize = 36.sp, color = theme.mainInk)
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = theme.mainInk.copy(alpha = 0.5f), spotColor = theme.mainInk)) {
                        OutlinedTextField(
                            value = searchQuery, onValueChange = { searchQuery = it },
                            placeholder = { Text("Search stories", color = theme.secondaryInk) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = theme.secondaryInk) },
                            trailingIcon = { Icon(Icons.Default.Mic, null, tint = theme.mainInk) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = theme.paperElevated, focusedContainerColor = theme.paperElevated, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Latest world", style = MaterialTheme.typography.displayLarge, fontSize = 28.sp, color = theme.mainInk)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Показуємо найновіший світ з бази даних
                    val latestWorld = worlds.firstOrNull()

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp).clickable {
                            latestWorld?.let { navController.navigate("world_detail/${it.name}") }
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .paint(painterResource(id = R.drawable.paper_texture), contentScale = ContentScale.Crop)
                                .background(Color.White.copy(alpha = 0.6f))
                        ) {
                            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = theme.mainInk)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("World:", fontWeight = FontWeight.Bold, color = theme.mainInk)
                                    Text(latestWorld?.name ?: "No worlds yet", fontWeight = FontWeight.Bold, color = theme.mainInk, maxLines = 1)
                                }
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF3EAD3).copy(alpha = 0.85f), modifier = Modifier.padding(end = 8.dp)) {
                                    Text("Open", fontSize = 12.sp, color = theme.mainInk, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = theme.dividerStrong, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("My Worlds", style = MaterialTheme.typography.displayLarge, fontSize = 28.sp, color = theme.mainInk)
                    Text("Manage and organize your writing projects", fontSize = 14.sp, color = theme.secondaryInk)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // СПИСОК УСІХ СВІТІВ З БД
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val filteredWorlds = worlds.filter { it.name.contains(searchQuery, ignoreCase = true) }

                    if (filteredWorlds.isEmpty() && worlds.isNotEmpty()) {
                        item { Text("No worlds found for '$searchQuery'", color = theme.secondaryInk) }
                    }

                    items(filteredWorlds, key = { it.id }) { world ->
                        OutlinedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
                            border = BorderStroke(1.dp, theme.dividerStrong),
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate("world_detail/${world.name}") }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = theme.mainInk)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = "World: ${world.name}", fontWeight = FontWeight.Medium, color = theme.mainInk, modifier = Modifier.weight(1f))

                                Box {
                                    IconButton(onClick = { expandedWorldId = if (expandedWorldId == world.id) null else world.id }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.MoreVert, null, tint = theme.mainInk)
                                    }

                                    DropdownMenu(expanded = expandedWorldId == world.id, onDismissRequest = { expandedWorldId = null }, containerColor = theme.paperElevated) {
                                        DropdownMenuItem(text = { Text("Edit") }, onClick = { expandedWorldId = null })
                                        DropdownMenuItem(text = { Text("Pin") }, onClick = { expandedWorldId = null })
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = Color.Red) },
                                            onClick = {
                                                db.collection("worlds").document(world.id).delete() // Видалення з БД
                                                expandedWorldId = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // 2. ЕФЕКТ ЗАТЕМНЕННЯ ФОНУ
        if (isFabExpanded) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { isFabExpanded = false })
        }

        // 3. FAB ТА МЕНЮ
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 16.dp)) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { 60 }, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.4f, transformOrigin = TransformOrigin(1f, 1f), animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)),
                exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { 60 }, animationSpec = tween(150)) + scaleOut(targetScale = 0.4f, transformOrigin = TransformOrigin(1f, 1f), animationSpec = tween(150)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD54F)), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            showCreateWorldDialog = true // ВІДКРИВАЄМО ДІАЛОГ
                        }.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = theme.mainInk)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New World", color = theme.mainInk, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.TopEnd) {
                Surface(
                    onClick = { isFabExpanded = !isFabExpanded }, shape = RoundedCornerShape(fabCorner), color = fabColor,
                    shadowElevation = if (isFabExpanded) 2.dp else 6.dp, modifier = Modifier.size(fabSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.rotate(fabRotation))
                    }
                }
            }
        }
    }
}