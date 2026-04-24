package com.zaitsev.liberink.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zaitsev.liberink.R
import com.zaitsev.liberink.models.NoteItem
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NotesScreen(navController: NavController) {
    val theme = LiberInkTheme.colors
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Стани для меню та даних
    var isMenuExpanded by remember { mutableStateOf(false) }
    val notes = remember { mutableStateListOf<NoteItem>() }
    var isLoading by remember { mutableStateOf(true) }

    // Firestore Realtime Listener
    DisposableEffect(currentUser) {
        val listenerRegistration = if (currentUser != null) {
            db.collection("notes")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Error fetching notes", error)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val fetchedNotes = snapshot.toObjects(NoteItem::class.java)
                        notes.clear()
                        notes.addAll(fetchedNotes)
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
            null
        }
        onDispose { listenerRegistration?.remove() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF6F3E9)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
            ) {
                SearchBarSection()
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = theme.mainInk)
                    }
                } else if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No notes yet.", color = Color.Gray)
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(notes, key = { it.id.ifEmpty { it.timestamp.toString() } }) { note ->
                            Box(
                                modifier = Modifier.clickable {
                                    val encodedTitle = URLEncoder.encode(note.title, StandardCharsets.UTF_8.toString())
                                    val encodedContent = URLEncoder.encode(note.content, StandardCharsets.UTF_8.toString())
                                    navController.navigate("create_note?id=${note.id}&title=$encodedTitle&content=$encodedContent")
                                }
                            ) {
                                when (note.type) {
                                    "quote" -> NoteQuoteCard(note.title, note.content)
                                    "sketch" -> NoteSketchCard(note.title, R.drawable.paper_texture)
                                    else -> NoteTextCard(note.title, note.content)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- UI ШАР: ЗАТЕМНЕННЯ ТА МЕНЮ ---

        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isMenuExpanded = false }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = isMenuExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Тільки одна кнопка з твоєю іконкою
                    ActionMenuItem(
                        label = "Text",
                        icon = painterResource(id = R.drawable.ic_text)
                    ) {
                        isMenuExpanded = false
                        navController.navigate("create_note")
                    }
                }
            }

            // ЗМІНЕНО: Головна кнопка тепер закруглений квадрат (RoundedCornerShape)
            FloatingActionButton(
                onClick = { isMenuExpanded = !isMenuExpanded },
                containerColor = Color(0xFFF5A623),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Expand Menu",
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(if (isMenuExpanded) 45f else 0f)
                )
            }
        }
    }
}

@Composable
fun ActionMenuItem(label: String, icon: Painter, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(48.dp)
            .widthIn(min = 120.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFE7C1),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color(0xFF4A142C),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A142C)
            )
        }
    }
}

// --- КАРТКИ НОТАТОК ---

@Composable
fun NoteTextCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (title.isNotBlank()) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4A142C))
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(text = content, fontSize = 12.sp, color = Color.DarkGray, maxLines = 10, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun NoteQuoteCard(title: String, quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEDACC))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title.ifBlank { "Quote" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp)
            Text(text = "“$quote”", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color(0xFF4A142C))
        }
    }
}

@Composable
fun NoteSketchCard(title: String, imageRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            if (title.isNotBlank()) {
                Text(text = title, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4A142C))
            }
        }
    }
}