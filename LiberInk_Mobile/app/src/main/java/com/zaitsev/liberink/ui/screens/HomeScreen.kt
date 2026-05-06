package com.zaitsev.liberink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import com.zaitsev.liberink.models.Book
import com.zaitsev.liberink.data.RetrofitClient
import com.zaitsev.liberink.R
import kotlinx.coroutines.launch
import com.zaitsev.liberink.ui.theme.ThemeManager

data class StoryInfo(
    val id: String,
    val title: String,
    val date: String,
    val draftCount: Int,
    val coverColor: Color
)

val CoverRed = Color(0xFFBC2626)
val CoverBlue = Color(0xFF7B90A5)

@Composable
fun HomeScreen(navController: NavController) {
    val theme = LiberInkTheme.colors
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val userId = user?.uid ?: ""
    val userName = user?.displayName?.split(" ")?.get(0) ?: "User"
    val photoUrl = user?.photoUrl?.toString()

    var searchQuery by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    var serverStories by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDeleteId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                val response = RetrofitClient.instance.getBooks(userId)
                serverStories = response
            } catch (e: Exception) {
                android.util.Log.e("LIBERINK_DEBUG", "Error fetching stories: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val removeBook = { bookId: Int ->
        scope.launch {
            try {
                val response = RetrofitClient.instance.deleteBook(bookId)
                if (response.isSuccessful) {
                    serverStories = serverStories.filter { it.id != bookId }
                    android.util.Log.d("LIBERINK_DEBUG", "Книгу видалено: $bookId")
                }
            } catch (e: Exception) {
                android.util.Log.e("LIBERINK_DEBUG", "Помилка видалення: ${e.message}")
            }
        }
    }

    val filteredStories = serverStories.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = LiberInkTheme.colors.paperMain
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopBarSection(userName = userName, photoUrl = photoUrl, navController = navController)
            SearchBarSection(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val mockDraft = StoryInfo(
                id = "draft_1",
                title = "Morning on Lake Prada: red breath",
                date = "Oct 26, 2026",
                draftCount = 3,
                coverColor = CoverRed
            )

            LatestDraftCard(story = mockDraft)

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "My Stories",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 28.sp,
                    color = LiberInkTheme.colors.mainInk
                )
                Text(
                    text = "Manage and organize your writing projects",
                    fontSize = 14.sp,
                    color = LiberInkTheme.colors.secondaryInk
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = theme.mainInk)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = filteredStories, key = { it.id }) { book ->
                        val displayStory = StoryInfo(
                            id = book.id.toString(),
                            title = book.title,
                            date = "Apr 22, 2026",
                            draftCount = 1,
                            coverColor = CoverBlue
                        )

                        StoryGridCard(
                            story = displayStory,
                            onDelete = {
                                bookToDeleteId = book.id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(text = "Delete story?", color = LiberInkTheme.colors.mainInk) },
                    text = { Text("This action cannot be undone. Are you sure you want to delete this manuscript?", color = LiberInkTheme.colors.mainInk) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                bookToDeleteId?.let { id -> removeBook(id) }
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", color = LiberInkTheme.colors.mainInk)
                        }
                    },
                    containerColor = LiberInkTheme.colors.paperElevated,
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
    }
}

@Composable
fun TopBarSection(userName: String, photoUrl: String?, navController: NavController) {
    val theme = LiberInkTheme.colors

    var expanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hello, $userName",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 36.sp,
            color = LiberInkTheme.colors.mainInk
        )

        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(theme.accentGold.copy(alpha = 0.2f))
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center
            ) {
                if (!photoUrl.isNullOrEmpty() && photoUrl != "null") {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = theme.mainInk
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(LiberInkTheme.colors.paperElevated)
            ) {
                DropdownMenuItem(
                    text = { Text("Account", color = theme.mainInk) },
                    onClick = {
                        expanded = false
                        navController.navigate("profile") {
                            popUpTo ("home")
                            launchSingleTop = true
                        }
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (ThemeManager.isDarkTheme) "Light paper" else "Night mode",
                            color = LiberInkTheme.colors.mainInk
                        )
                    },
                    onClick = {
                        ThemeManager.toggleTheme(context)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(
                                id = if (ThemeManager.isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon
                            ),
                            contentDescription = null,
                            tint = LiberInkTheme.colors.mainInk
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text("About App", color = theme.mainInk) },
                    onClick = {
                        expanded = false
                        showAboutDialog = true
                    }
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About LiberInk", color = theme.mainInk) },
            text = {
                Column {
                    Text(
                        "LiberInk is your personal creative sanctuary. Craft immersive worlds, structure your lore, and write stories without boundaries.",
                        color = theme.mainInk,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Real-time draft synchronization\n" +
                                "• Dedicated tools for deep lore building\n" +
                                "• Night mode support for late-night inspiration",
                        color = theme.secondaryInk,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Version: 1.0.0 (Beta)\nDeveloped by Nazar\nSpecialty: Software Engineering",
                        color = theme.secondaryInk.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = theme.mainInk)
                }
            },
            containerColor = LiberInkTheme.colors.paperElevated,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun SearchBarSection(query: String, onQueryChange: (String) -> Unit) {
    val theme = LiberInkTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .shadow(4.dp, RoundedCornerShape(25.dp))
                .background(theme.paperElevated, RoundedCornerShape(25.dp))
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text("Search stories", color = theme.secondaryInk.copy(alpha = 0.5f))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = theme.secondaryInk,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = theme.mainInk,
                    focusedTextColor = theme.mainInk,
                    unfocusedTextColor = theme.mainInk
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun LatestDraftCard(story: StoryInfo) {
    val theme = LiberInkTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(theme.paperElevated, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCover(color = story.coverColor, title = story.title, modifier = Modifier.size(70.dp, 100.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = story.title, fontWeight = FontWeight.Bold, color = theme.mainInk, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "82096 words", fontSize = 12.sp, color = theme.secondaryInk)
                    DraftBadge(count = story.draftCount)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(100.dp)
                    .background(theme.paperMain, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Ch. 1", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.mainInk)
                    Text(text = "Oct 26", fontSize = 10.sp, color = theme.secondaryInk)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "2 Drafts", fontSize = 10.sp, color = theme.mainInk, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryGridCard(story: StoryInfo, onDelete: () -> Unit) {
    val theme = LiberInkTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(LiberInkTheme.colors.paperElevated, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(
                color = story.coverColor,
                title = story.title,
                modifier = Modifier.size(50.dp, 75.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp)
            ) {
                Text(
                    text = story.title,
                    fontWeight = FontWeight.Bold,
                    color = theme.mainInk,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = story.date,
                    fontSize = 10.sp,
                    color = theme.secondaryInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                DraftBadge(count = story.draftCount)
            }
        }

        Box(
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            StoryCardMenu(themeColors = theme, onDelete = onDelete)
        }
    }
}

@Composable
fun StoryCardMenu(themeColors: com.zaitsev.liberink.ui.theme.LiberInkColors, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = themeColors.mainInk.copy(alpha = 0.5f)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(LiberInkTheme.colors.paperElevated)
        ) {
            DropdownMenuItem(
                text = { Text("Edit", color = themeColors.mainInk) },
                onClick = { expanded = false },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = themeColors.mainInk) }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = themeColors.mainInk) },
                onClick = {
                    expanded = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = themeColors.mainInk) }
            )
        }
    }
}

@Composable
fun BookCover(color: Color, title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
            .background(color, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(Color.Black.copy(alpha = 0.15f))
        )
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 7.sp,
            lineHeight = 9.sp,
            modifier = Modifier.padding(start = 8.dp, top = 6.dp, end = 2.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DraftBadge(count: Int) {
    val theme = LiberInkTheme.colors
    Row(
        modifier = Modifier
            .background(theme.accentGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count Drafts",
            fontSize = 9.sp,
            color = LiberInkTheme.colors.mainInk,
            fontWeight = FontWeight.Bold
        )
    }
}