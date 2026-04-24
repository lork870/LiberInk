package com.zaitsev.liberink.ui.screens

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.zaitsev.liberink.R
import com.zaitsev.liberink.ui.components.AppHeader
import com.zaitsev.liberink.ui.theme.DarkWine
import com.zaitsev.liberink.ui.theme.DarkWine20
import com.zaitsev.liberink.ui.theme.DarkWine50
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor
import com.zaitsev.liberink.ui.Screen

@Composable
fun RegistrationScreen(navController: NavController, noteId: String? = null) {
    val theme = LiberInkTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var googleAccountName by remember { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    val animatedOffset by animateDpAsState(if (isPressed) 4.dp else 0.dp, label = "offset")

    if (isSuccess) {
        LaunchedEffect(Unit) {
            delay(2000)
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.paperMain),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = theme.mainInk
            )

            if (googleAccountName != null) {
                Surface(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Signed in as $googleAccountName", color = Color(0xFF2E7D32), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text("Start writing your story today", fontSize = 16.sp, color = theme.secondaryInk)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                        drawRoundRect(0f, 0f, size.width, size.height, 28.dp.toPx(), 28.dp.toPx(), paint)
                    }
                }
                .clip(RoundedCornerShape(28.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.paper_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )

            Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.6f)))

            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Crossfade(targetState = isSuccess, label = "Success Fade") { success ->
                    if (success) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 32.dp)
                        ) {
                            Box(modifier = Modifier.size(72.dp).background(Color(0xFF4CAF50), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Welcome, ${googleAccountName ?: name}!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkWine)
                            Text("Redirecting to library...", fontSize = 16.sp, color = Color.Gray)
                        }
                    } else {
                        Column {
                            OutlinedTextField(
                                value = name, onValueChange = { name = it },
                                label = { Text("Name *") },
                                placeholder = { Text("Your Name", color = DarkWine20) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.mainInk)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = email, onValueChange = { email = it },
                                label = { Text("Email *") },
                                placeholder = { Text("You@example.com", color = DarkWine20) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.mainInk)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = password, onValueChange = { password = it },
                                label = { Text("Password *") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.mainInk)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = {
                                    if (name.isBlank() || email.isBlank() || password.length < 6) {
                                        Toast.makeText(context, "Check your inputs (Password min 6 chars)", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnSuccessListener { result ->
                                            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest { displayName = name }
                                            result.user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                                val uid = result.user?.uid
                                                if (noteId != null && uid != null) {
                                                    db.collection("onboarding_notes").document(noteId)
                                                        .update("ownerId", uid, "author_name", name, "isTemporary", false)
                                                        .addOnCompleteListener {
                                                            isLoading = false
                                                            isSuccess = true
                                                        }
                                                } else {
                                                    isLoading = false
                                                    isSuccess = true
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                enabled = !isLoading,
                                interactionSource = interactionSource,
                                modifier = Modifier.fillMaxWidth().height(56.dp).graphicsLayer { scaleX = scale; scaleY = scale; translationY = animatedOffset.toPx() },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk, contentColor = theme.paperElevated),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = theme.paperElevated, strokeWidth = 2.dp)
                                else Text("Create Account", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = theme.dividerStrong)
                                Text(" Or continue with ", fontSize = 14.sp, color = theme.secondaryInk, modifier = Modifier.padding(horizontal = 8.dp))
                                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = theme.dividerStrong)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedButton(
                                onClick = {
                                    signUpWithGoogle(
                                        context = context,
                                        scope = scope,
                                        noteId = noteId,
                                        onLoading = { isLoading = it },
                                        onSuccess = { resultName ->
                                            googleAccountName = resultName
                                            isSuccess = true
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.dp, color = DarkWine50),
                                enabled = !isLoading
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Sign up with Google", fontSize = 16.sp, color = DarkWine, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isSuccess) {
            TextButton(onClick = { navController.navigate("authorization") }, modifier = Modifier.padding(bottom = 32.dp)) {
                Text(text = "Don't have an account? ", color = Color.Gray, fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge)

                Text(
                    text = "Sign in",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = theme.mainInk),
                    fontSize = 18.sp,
                    modifier = Modifier.drawBehind {
                        drawLine(color = theme.mainInk, start = Offset(0f, size.height + 4.dp.toPx()), end = Offset(size.width, size.height + 4.dp.toPx()), strokeWidth = 1.dp.toPx())
                    }
                )
            }
        }
    }
}

fun signUpWithGoogle(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    noteId: String?,
    onLoading: (Boolean) -> Unit,
    onSuccess: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val webClientId = "74885674202-j2k0f2ijc8crcghkfibpb7jlrc1bs58a.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                onLoading(true)
                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid
                        val userName = authResult.user?.displayName ?: "Author"

                        if (noteId != null && uid != null) {
                            db.collection("onboarding_notes").document(noteId)
                                .update("ownerId", uid, "author_name", userName, "isTemporary", false)
                                .addOnCompleteListener {
                                    onLoading(false)
                                    onSuccess(userName)
                                }
                        } else {
                            onLoading(false)
                            onSuccess(userName)
                        }
                    }
                    .addOnFailureListener { e ->
                        onLoading(false)
                        Toast.makeText(context, "Firebase Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        } catch (e: Exception) {
            onLoading(false)
            if (e !is GetCredentialCancellationException) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}