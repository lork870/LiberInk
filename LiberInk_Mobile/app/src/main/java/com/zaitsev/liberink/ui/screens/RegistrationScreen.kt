package com.zaitsev.liberink.ui.screens

import android.graphics.Paint
import android.util.Log
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
import androidx.credentials.exceptions.NoCredentialException
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

            Spacer(modifier = Modifier.height(8.dp))
            Text("Start writing your story today", fontSize = 16.sp, color = theme.secondaryInk)
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
                        SuccessContent(googleAccountName ?: name)
                    } else {
                        RegistrationForm(
                            name = name, onNameChange = { name = it },
                            email = email, onEmailChange = { email = it },
                            password = password, onPasswordChange = { password = it },
                            isLoading = isLoading,
                            theme = theme,
                            interactionSource = interactionSource,
                            scale = scale,
                            animatedOffset = animatedOffset,
                            onCreateClick = {
                                if (name.isBlank() || email.isBlank() || password.length < 6) {
                                    Toast.makeText(context, "Check your inputs!", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnSuccessListener { result ->
                                            val uid = result.user?.uid
                                            if (noteId != null && uid != null) {
                                                transferNote(noteId, uid) {
                                                    isLoading = false
                                                    isSuccess = true
                                                }
                                            } else {
                                                isLoading = false
                                                isSuccess = true
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                                        }
                                }
                            },
                            onGoogleClick = {
                                signUpWithGoogle(context, scope, noteId, { isLoading = it }) { resultName ->
                                    googleAccountName = resultName
                                    isSuccess = true
                                }
                            }
                        )
                    }
                }
            }
        }

        if (!isSuccess) {
            SignInFooter(navController, theme)
        }
    }
}

@Composable
fun SuccessContent(userName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        Box(modifier = Modifier.size(72.dp).background(Color(0xFF4CAF50), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome, $userName!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkWine)
        Text("Redirecting to library...", fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
fun RegistrationForm(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    theme: com.zaitsev.liberink.ui.theme.LiberInkColors,
    interactionSource: MutableInteractionSource,
    scale: Float,
    animatedOffset: androidx.compose.ui.unit.Dp,
    onCreateClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.mainInk)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.mainInk)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = onPasswordChange,
            label = { Text("Password *") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.mainInk)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateClick,
            enabled = !isLoading,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth().height(56.dp).graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = animatedOffset.toPx()
            },
            colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = theme.paperElevated, strokeWidth = 2.dp)
            else Text("Create Account", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = theme.dividerStrong)
            Text(" Or continue with ", fontSize = 14.sp, color = theme.secondaryInk, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = theme.dividerStrong)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, color = DarkWine50),
            enabled = !isLoading
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sign up with Google", color = DarkWine, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SignInFooter(navController: NavController, theme: com.zaitsev.liberink.ui.theme.LiberInkColors) {
    TextButton(onClick = { navController.navigate("authorization") }, modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)) {
        Text("Already have an account? ", color = Color.Gray, fontSize = 18.sp)
        Text("Sign in", color = theme.mainInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
fun transferNote(tempNoteId: String, uid: String, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("onboarding_notes").document(tempNoteId).get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val data = snapshot.data?.toMutableMap() ?: mutableMapOf()

                val penName = data["author_pen_name"] ?: ""
                val bio = data["author_bio"] ?: ""

                val userData = hashMapOf(
                    "penName" to penName,
                    "bio" to bio
                )
                db.collection("users").document(uid).set(userData, com.google.firebase.firestore.SetOptions.merge())

                data.remove("author_pen_name")
                data.remove("author_bio")

                data["userId"] = uid
                data["isTemporary"] = false

                if (data.containsKey("description")) {
                    data["content"] = data["description"]
                    data.remove("description") // Видаляємо старе поле
                }
                data["timestamp"] = System.currentTimeMillis()
                data["type"] = "text"

                db.collection("notes").add(data)
                    .addOnSuccessListener {
                        db.collection("onboarding_notes").document(tempNoteId).delete()
                        onComplete()
                    }
                    .addOnFailureListener { onComplete() }
            } else {
                onComplete()
            }
        }
        .addOnFailureListener { onComplete() }
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
                            transferNote(noteId, uid) {
                                onLoading(false)
                                onSuccess(userName)
                            }
                        } else {
                            onLoading(false)
                            onSuccess(userName)
                        }
                    }
            }
        } catch (e: Exception) {
            onLoading(false)
            if (e is NoCredentialException) {
                Toast.makeText(context, "No Google accounts found on device", Toast.LENGTH_LONG).show()
            } else if (e !is GetCredentialCancellationException) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}