package com.zaitsev.liberink.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.TransformOrigin
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

    var isMenuExpanded by remember { mutableStateOf(false) }
    val notes = remember { mutableStateListOf<NoteItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") } // Стан для пошуку

    val fabRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 225f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "fab_rotation"
    )
    val fabSize by animateDpAsState(
        targetValue = if (isMenuExpanded) 44.dp else 64.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "fab_size"
    )
    val fabCorner by animateDpAsState(
        targetValue = if (isMenuExpanded) 22.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "fab_corner"
    )
    val fabColor by animateColorAsState(
        targetValue = if (isMenuExpanded) Color(0xFFCC8500) else Color(0xFFF6A000), // Можеш змінити на theme.accentGold
        animationSpec = tween(200),
        label = "fab_color"
    )

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

    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = theme.paperMain
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
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
                                    "sketch" -> NoteSketchCard(note.title, R.drawable.paper_texture, theme) // TODO: Замінити на реальне зображення
                                    else -> NoteTextCard(note.title, note.content, theme)
                                }
                            }
                        }
                    }
                }
            }
        }

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
            horizontalAlignment = Alignment.End
        ) {

            AnimatedVisibility(
                visible = isMenuExpanded,
                enter = fadeIn(tween(200)) +
                        slideInVertically(
                            initialOffsetY = { 60 },
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)
                        ) +
                        scaleIn(
                            initialScale = 0.4f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
                        ),
                exit = fadeOut(tween(150)) +
                        slideOutVertically(targetOffsetY = { 60 }, animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.4f, transformOrigin = TransformOrigin(1f, 1f), animationSpec = tween(150)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Можеш додати більше кнопок сюди (Sketch, Audio тощо)
                    ActionMenuItem(
                        label = "Text Note",
                        icon = painterResource(id = R.drawable.ic_text), // Переконайся що іконка існує
                        themeColors = theme
                    ) {
                        isMenuExpanded = false
                        navController.navigate("create_note")
                    }
                }
            }

            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    onClick = { isMenuExpanded = !isMenuExpanded },
                    shape = RoundedCornerShape(fabCorner),
                    color = fabColor,
                    shadowElevation = if (isMenuExpanded) 2.dp else 6.dp,
                    modifier = Modifier.size(fabSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.rotate(fabRotation)
                        )
                    }
                }
            }
        }
    }
}

// ... Інші компоненти (NotesSearchBar, ActionMenuItem, NoteTextCard, etc.) залишаються без змін ...

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