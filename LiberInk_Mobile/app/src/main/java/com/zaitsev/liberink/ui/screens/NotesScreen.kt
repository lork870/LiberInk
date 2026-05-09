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
import androidx.compose.ui.draw.shadow
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

    // Стани
    var isMenuExpanded by remember { mutableStateOf(false) }
    val notes = remember { mutableStateListOf<NoteItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") } // Стан для пошуку

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
                        val fetchedNotes = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(NoteItem::class.java)?.copy(id = doc.id)
                        }
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

    // Фільтрація нотаток за пошуковим запитом
    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = theme.paperMain // Адаптивний фон
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Додаємо робоче поле пошуку
                NotesSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = theme.mainInk)
                    }
                } else if (filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No notes yet." else "Nothing found.",
                            color = theme.secondaryInk
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(filteredNotes, key = { it.id.ifEmpty { it.timestamp.toString() } }) { note ->
                            Box(
                                modifier = Modifier
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.paperElevated)
                                    .clickable {
                                        val encodedTitle = URLEncoder.encode(note.title, StandardCharsets.UTF_8.toString())
                                        val encodedContent = URLEncoder.encode(note.content, StandardCharsets.UTF_8.toString())
                                        navController.navigate("create_note?id=${note.id}&title=$encodedTitle&content=$encodedContent")
                                    }
                            ) {
                                when (note.type) {
                                    "quote" -> NoteQuoteCard(note.title, note.content, theme)
                                    "sketch" -> NoteSketchCard(note.title, R.drawable.paper_texture, theme)
                                    else -> NoteTextCard(note.title, note.content, theme)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Шар затемнення при відкритому меню
        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isMenuExpanded = false }
            )
        }

        // FAB та меню
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
                    ActionMenuItem(
                        label = "Text Note",
                        icon = painterResource(id = R.drawable.ic_text),
                        themeColors = theme
                    ) {
                        isMenuExpanded = false
                        navController.navigate("create_note")
                    }
                }
            }

            FloatingActionButton(
                onClick = { isMenuExpanded = !isMenuExpanded },
                containerColor = theme.accentGold, // Використовуємо колір теми
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(if (isMenuExpanded) 45f else 0f)
                )
            }
        }
    }
}

@Composable
fun NotesSearchBar(query: String, onQueryChange: (String) -> Unit) {
    val theme = LiberInkTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(25.dp))
            .background(theme.paperElevated, RoundedCornerShape(25.dp))
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search notes...", color = theme.secondaryInk.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_search), null, tint = theme.secondaryInk, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = theme.mainInk,
                focusedTextColor = theme.mainInk,
                unfocusedTextColor = theme.mainInk
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ActionMenuItem(label: String, icon: Painter, themeColors: com.zaitsev.liberink.ui.theme.LiberInkColors, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(48.dp)
            .widthIn(min = 140.dp),
        shape = RoundedCornerShape(24.dp),
        color = themeColors.paperElevated,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = icon, contentDescription = null, tint = themeColors.mainInk, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = themeColors.mainInk)
        }
    }
}

// --- АДАПТИВНІ КАРТКИ ---

@Composable
fun NoteTextCard(title: String, content: String, theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    Column(modifier = Modifier.padding(12.dp)) {
        if (title.isNotBlank()) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.mainInk)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(text = content, fontSize = 12.sp, color = theme.secondaryInk, maxLines = 10, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun NoteQuoteCard(title: String, quote: String, theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = title.ifBlank { "Quote" }, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = theme.secondaryInk.copy(alpha = 0.6f))
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = theme.mainInk.copy(alpha = 0.1f))
        Text(text = "“$quote”", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = theme.mainInk)
    }
}

@Composable
fun NoteSketchCard(title: String, imageRes: Int, theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    Column {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        )
        if (title.isNotBlank()) {
            Text(text = title, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.mainInk)
        }
    }
}