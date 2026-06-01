package com.zaitsev.liberink.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
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
fun WorldDetailScreen(navController: NavController, worldName: String?) {
    val theme = LiberInkTheme.colors
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val currentWorldName = worldName?.replace("%20", " ") ?: "The Valley of Seven Thunders"

    var selectedCategory by remember { mutableStateOf("Locations") }
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }

    // Список реальних даних з Firebase
    val loreItems = remember { mutableStateListOf<LoreItem>() }
    var isLoading by remember { mutableStateOf(true) }

    // ЧИТАННЯ ДАНИХ З FIRESTORE У РЕАЛЬНОМУ ЧАСІ
    DisposableEffect(currentUser, currentWorldName, selectedCategory) {
        val listener = if (currentUser != null) {
            db.collection("lore_items")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("worldName", currentWorldName)
                .whereEqualTo("type", selectedCategory)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Error fetching lore items", error)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val fetched = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(LoreItem::class.java)?.copy(id = doc.id)
                        }.sortedByDescending { it.timestamp } // Сортуємо від нових до старих

                        loreItems.clear()
                        loreItems.addAll(fetched)
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
            null
        }
        onDispose { listener?.remove() }
    }

    // ФІЗИКА АНІМАЦІЙ FAB
    val fabRotation by animateFloatAsState(targetValue = if (isFabMenuOpen) 225f else 0f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f), label = "fab_rotation")
    val fabSize by animateDpAsState(targetValue = if (isFabMenuOpen) 44.dp else 56.dp, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f), label = "fab_size")
    val fabCorner by animateDpAsState(targetValue = if (isFabMenuOpen) 22.dp else 16.dp, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f), label = "fab_corner")
    val fabColor by animateColorAsState(targetValue = if (isFabMenuOpen) Color(0xFFCC8500) else Color(0xFFF6A000), animationSpec = tween(200), label = "fab_color")

    val categories = listOf(
        CategoryItem("Locations", Icons.Default.Public),
        CategoryItem("Character", Icons.Default.Person), // Змінив на "Character" щоб співпадало з БД
        CategoryItem("Cultures", Icons.Default.MenuBook),
        CategoryItem("Magic", Icons.Default.AutoAwesome),
        CategoryItem("History", Icons.Default.Timeline)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. ГОЛОВНИЙ ЕКРАН (БЕЗ FAB)
        Scaffold(
            containerColor = theme.paperMain
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "World: $currentWorldName",
                    style = MaterialTheme.typography.displayLarge, fontSize = 28.sp, color = theme.mainInk,
                    modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.mainInk)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = "", onValueChange = {},
                        placeholder = { Text("Search stories", color = theme.secondaryInk, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = theme.secondaryInk) },
                        trailingIcon = { Icon(Icons.Default.SwapVert, null, tint = theme.mainInk) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = theme.paperElevated, focusedContainerColor = theme.paperElevated, unfocusedBorderColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6DBCB)),
                        modifier = Modifier.size(50.dp).clickable { isGridView = !isGridView }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(if (isGridView) Icons.Default.ViewList else Icons.Default.GridView, null, tint = theme.mainInk)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category.name
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE6BEB3) else theme.paperElevated),
                            modifier = Modifier.clickable { selectedCategory = category.name }.padding(vertical = 4.dp).height(80.dp).width(70.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(category.icon, null, tint = theme.mainInk)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(category.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.mainInk)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = selectedCategory, fontSize = 28.sp, color = theme.mainInk, style = MaterialTheme.typography.displayMedium)
                Text(text = "Items: ${loreItems.size}", fontSize = 14.sp, color = theme.secondaryInk)
                Spacer(modifier = Modifier.height(16.dp))

                // РЕНДЕР ДАНИХ З БАЗИ
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = theme.mainInk)
                    }
                } else if (loreItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No $selectedCategory yet. Tap + to create one.", color = theme.secondaryInk)
                    }
                } else {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(loreItems, key = { it.id }) { item ->
                                LoreGridItemCard(item, theme) { navController.navigate("lore_detail/${item.id}") }
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                            items(loreItems, key = { it.id }) { item ->
                                LoreListItemCard(item, theme) { navController.navigate("lore_detail/${item.id}") }
                            }
                        }
                    }
                }
            }
        }

        // 2. ЗАТЕМНЕННЯ ФОНУ
        if (isFabMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isFabMenuOpen = false }
            )
        }

        // 3. FAB ТА МЕНЮ
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
        ) {
            AnimatedVisibility(
                visible = isFabMenuOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 60 }) + scaleIn(initialScale = 0.4f, transformOrigin = TransformOrigin(1f, 1f)),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 60 }) + scaleOut(targetScale = 0.4f, transformOrigin = TransformOrigin(1f, 1f)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FabMenuItem("Locations", Icons.Default.Public, theme.mainInk) { isFabMenuOpen = false; navController.navigate("create_lore/Locations") }
                    FabMenuItem("Character", Icons.Default.Person, theme.mainInk) { isFabMenuOpen = false; navController.navigate("create_lore/Character") }
                    FabMenuItem("Culture Note", Icons.Default.MenuBook, theme.mainInk) { isFabMenuOpen = false }
                }
            }

            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.TopEnd) {
                Surface(
                    onClick = { isFabMenuOpen = !isFabMenuOpen },
                    shape = RoundedCornerShape(fabCorner), color = fabColor,
                    shadowElevation = if (isFabMenuOpen) 2.dp else 6.dp,
                    modifier = Modifier.size(fabSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.rotate(fabRotation))
                    }
                }
            }
        }
    }
}

// Карточка для Списку (приймає LoreItem)
@Composable
fun LoreListItemCard(item: LoreItem, theme: com.zaitsev.liberink.ui.theme.LiberInkColors, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(100.dp).fillMaxHeight().background(Color.LightGray))
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Text(item.name.ifBlank { "Untitled" }, fontWeight = FontWeight.Bold, color = theme.mainInk, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.bio.ifBlank { "No description available." }, fontSize = 12.sp, color = theme.secondaryInk, maxLines = 2)
            }
        }
    }
}

// Карточка для Сітки (приймає LoreItem)
@Composable
fun LoreGridItemCard(item: LoreItem, theme: com.zaitsev.liberink.ui.theme.LiberInkColors, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
        modifier = Modifier.aspectRatio(0.85f).clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
            Text(
                text = item.name.ifBlank { "Untitled" },
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
            )
        }
    }
}

@Composable
fun FabMenuItem(text: String, icon: ImageVector, contentColor: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD54F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector)