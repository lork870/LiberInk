package com.zaitsev.liberink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@Composable
fun LoreItemDetailScreen(navController: NavController, itemId: String) {
    val theme = LiberInkTheme.colors
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = theme.paperMain,
        bottomBar = { DetailBottomBar(theme) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 1. ФОНОВЕ ЗОБРАЖЕННЯ (Верхня частина) ---
            // Coil AsyncImage для завантаження реальних фото.
            // Поки немає бекенду, ставимо сірий фон або плейсхолдер.
            AsyncImage(
                model = "https://images.unsplash.com/photo-1599939571322-792a326cb915?q=80&w=1000", // Плейсхолдер замку
                contentDescription = "Lore Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp) // Висота картинки
                    .background(Color.Gray)
            )

            // --- 2. КОНТЕНТ (Скролиться і наїжджає на фото) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Цей Spacer визначає, скільки фотографії буде видно до того, як почнеться картка
                Spacer(modifier = Modifier.height(340.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 500.dp), // Щоб картка тягнулась до низу екрана
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = theme.paperMain,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        // Заголовок (Шрифт як у дизайні)
                        Text(
                            text = "Citadel of the Grail",
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 36.sp,
                            color = theme.mainInk,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        // --- СЕКЦІЇ ІНФОРМАЦІЇ ---

                        // 1. Опис та історія
                        InfoSection(
                            icon = Icons.Default.Description,
                            title = "Description and History",
                            theme = theme
                        ) {
                            Text(
                                text = "An ancient fortress perched atop a cliff, the Grail Citadel is the main bastion of the Order of the Knights of Etherium. Built over 400 years ago by the first High King, it contains detailed accounts of key towers, halls, and vaulted chambers...",
                                fontSize = 14.sp,
                                color = theme.mainInk.copy(alpha = 0.85f),
                                lineHeight = 22.sp
                            )
                        }

                        // 2. Теги / Символи
                        InfoSection(
                            icon = Icons.Default.Groups,
                            title = "Symbols and resources",
                            theme = theme
                        ) {
                            // FlowRow (або звичайний Row з wrap) для тегів
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                LoreChip("Cup and sword", theme)
                                LoreChip("Grail Water", theme)
                                LoreChip("Light crystals", theme)
                            }
                        }

                        // 3. Тип локації
                        InfoSection(
                            icon = Icons.Default.LocationOn,
                            title = "Type",
                            theme = theme
                        ) {
                            Text(
                                text = "Kingdom",
                                fontSize = 14.sp,
                                color = theme.mainInk.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }

            // --- 3. ЗАКРІПЛЕНА КНОПКА "НАЗАД" ---
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .size(40.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

// --- ДОПОМІЖНІ КОМПОНЕНТИ ---

@Composable
fun InfoSection(
    icon: ImageVector,
    title: String,
    theme: com.zaitsev.liberink.ui.theme.LiberInkColors,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.mainInk,
            modifier = Modifier.size(24.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = theme.mainInk
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun LoreChip(text: String, theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEFE0D9) // Пильно-рожевий/бежевий колір тегів з дизайну
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = theme.mainInk,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetailBottomBar(theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    Surface(
        color = Color(0xFFEBE0DD), // Колір нижньої панелі
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(), // Щоб не перекривалося системними кнопками
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ліві круглі кнопки
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE4C9C0), // Темніший рожевий для кнопок
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { /* Логіка редагування */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = theme.mainInk, modifier = Modifier.size(20.dp))
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE4C9C0),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { /* Логіка палітри/вигляду */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Palette, contentDescription = "Appearance", tint = theme.mainInk, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Права кнопка "New data"
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFE4C9C0),
                modifier = Modifier
                    .height(44.dp)
                    .clickable { /* Логіка додавання даних */ }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.AddBox, contentDescription = null, tint = theme.mainInk, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New data", color = theme.mainInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}