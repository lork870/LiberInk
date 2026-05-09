package com.zaitsev.liberink.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(navController: NavController) {
    val theme = LiberInkTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    val pagerState = rememberPagerState(pageCount = { 2 })

    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var pseudonym by remember { mutableStateOf("") }

    val memberSince = remember {
        user?.metadata?.creationTimestamp?.let {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Unknown"
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        pseudonym = doc.getString("penName") ?: ""
                    }
                }
        }
    }

    fun saveProfile() {
        val uid = user?.uid ?: return
        user.updateProfile(userProfileChangeRequest { displayName = name })
        val userData = hashMapOf("penName" to pseudonym)
        db.collection("users").document(uid).set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                isEditing = false
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            }
    }

    BackHandler(enabled = pagerState.currentPage == 1 || isEditing) {
        if (isEditing) {
            isEditing = false
        } else if (pagerState.currentPage == 1) {
            scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.paperMain)
            .statusBarsPadding()
    ) {
        // --- ФІКСОВАНА ШАПКА ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = theme.mainInk,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        if (pagerState.currentPage == 1) {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        } else {
                            navController.popBackStack()
                        }
                    }
            )

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, theme.dividerStrong),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.mainInk),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Log out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(text = "Account", style = MaterialTheme.typography.displayLarge, fontSize = 40.sp, color = theme.mainInk)
            Text(text = "Manage your account and view your statistics", fontSize = 14.sp, color = theme.secondaryInk, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> {
                    ProfileContent(
                        theme = theme,
                        name = name, onNameChange = { name = it },
                        pseudonym = pseudonym, onPseudonymChange = { pseudonym = it },
                        email = email,
                        memberSince = memberSince,
                        isEditing = isEditing,
                        onEditChange = { isEditing = it },
                        onSave = { saveProfile() },
                        onDeleteClick = { showDeleteDialog = true },
                        onViewStatisticsClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                }
                1 -> {
                    StatisticsContent(
                        theme = theme,
                        onViewAccountDataClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            theme = theme,
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                auth.signOut()
                navController.navigate("onboarding") { popUpTo(0) { inclusive = true } }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            theme = theme,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                        navController.navigate("onboarding") { popUpTo(0) { inclusive = true } }
                    } else {
                        if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                            Toast.makeText(context, "Please log out and log in again to delete account", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LogoutDialog(
    theme: com.zaitsev.liberink.ui.theme.LiberInkColors,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.paperElevated,
        title = { Text("Log Out", color = theme.mainInk, fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to log out of your sanctuary?", color = theme.mainInk) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk)
            ) { Text("Log Out", color = theme.paperElevated) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = theme.mainInk) }
        }
    )
}

@Composable
fun ProfileContent(
    theme: com.zaitsev.liberink.ui.theme.LiberInkColors,
    name: String, onNameChange: (String) -> Unit,
    pseudonym: String, onPseudonymChange: (String) -> Unit,
    email: String, memberSince: String,
    isEditing: Boolean, onEditChange: (Boolean) -> Unit,
    onSave: () -> Unit, onDeleteClick: () -> Unit,
    onViewStatisticsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Personal data", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = theme.mainInk)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
                        Box(modifier = Modifier.size(80.dp).background(Color(0xFFF5E0C3), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = "Avatar", tint = theme.mainInk, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* Change Avatar */ },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Change", fontSize = 12.sp, color = theme.paperElevated)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        ProfileTextField("Name", name, isEditing, onNameChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileTextField("Pseudonym", pseudonym, isEditing, onPseudonymChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileTextField("Email", email, false) {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Additional data", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.mainInk)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Member Since", fontSize = 12.sp, color = theme.secondaryInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF6EFE5), RoundedCornerShape(8.dp)).padding(12.dp, 8.dp)) {
                        Text(memberSince, fontSize = 14.sp, color = theme.mainInk)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { },
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change password", fontSize = 13.sp, color = theme.mainInk)
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = theme.mainInk, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Statistics", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.mainInk)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatRow("Books:", "4", theme)
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("Words:", "142,276", theme)
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("Time:", "1298 h", theme)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка для переходу на Пейдж 2 (Статистика)
        OutlinedButton(
            onClick = onViewStatisticsClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, theme.mainInk.copy(alpha = 0.5f))
        ) {
            Text("View all statistics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.mainInk)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = theme.mainInk)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (isEditing) {
                OutlinedButton(onClick = { onEditChange(false) }, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, theme.mainInk.copy(alpha = 0.5f)), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Cancel", fontSize = 16.sp, color = theme.mainInk)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onSave, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Save", fontSize = 16.sp, color = theme.paperElevated)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                }
            } else {
                OutlinedButton(onClick = onDeleteClick, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color(0xFFE57373).copy(alpha = 0.5f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373)), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Delete Account", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onEditChange(true) }, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Edit Profile", fontSize = 15.sp, color = theme.paperElevated)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatisticsContent(
    theme: com.zaitsev.liberink.ui.theme.LiberInkColors,
    onViewAccountDataClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Картка Календаря
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().height(320.dp) // Висота під календар
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Activity Calendar Placeholder", color = theme.secondaryInk)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Month Statistics", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.mainInk)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatRow("Books:", "1", theme)
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("Words:", "14,478", theme)
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("Time:", "120 h", theme)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = theme.paperElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.weight(0.7f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.mainInk)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatRow("", "4", theme)
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("", "142,276", theme)
                    HorizontalDivider(color = theme.dividerStrong, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("", "1298 h", theme)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка для повернення на Пейдж 1 (Профіль)
        OutlinedButton(
            onClick = onViewAccountDataClick,
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, theme.mainInk.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Стрілочка вліво
                contentDescription = null,
                tint = theme.mainInk
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("View account data", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.mainInk)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Загальні компоненти для полів і статистики
@Composable
fun ProfileTextField(label: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    val theme = LiberInkTheme.colors
    Column {
        Text(text = label, fontSize = 12.sp, color = theme.mainInk, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        if (isEditing) {
            OutlinedTextField(
                value = value, onValueChange = onValueChange, singleLine = true,
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF6EFE5), unfocusedContainerColor = Color(0xFFF6EFE5),
                    focusedBorderColor = theme.mainInk.copy(alpha = 0.5f), unfocusedBorderColor = Color(0xFFE6DBCB)
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = theme.mainInk)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFF6EFE5), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = value, fontSize = 14.sp, color = theme.mainInk)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 13.sp, color = theme.mainInk)
        Text(text = value, fontSize = 13.sp, color = theme.mainInk, fontWeight = FontWeight.Medium)
    }
}

// ДІАЛОГ ВИДАЛЕННЯ АКАУНТА
@Composable
fun DeleteAccountDialog(theme: com.zaitsev.liberink.ui.theme.LiberInkColors, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var confirmationWord by remember { mutableStateOf("") }
    val isConfirmed = confirmationWord.trim().equals("DELETE", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.paperElevated,
        title = { Text("Delete Account", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("This action is permanent and cannot be undone. All your notes, books, and statistics will be lost.", color = theme.mainInk, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Type DELETE to confirm:", color = theme.mainInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmationWord, onValueChange = { confirmationWord = it }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD32F2F), focusedTextColor = theme.mainInk),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm, enabled = isConfirmed,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), disabledContainerColor = Color(0xFFD32F2F).copy(alpha = 0.5f))
            ) { Text("Delete Forever", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = theme.mainInk) }
        }
    )
}