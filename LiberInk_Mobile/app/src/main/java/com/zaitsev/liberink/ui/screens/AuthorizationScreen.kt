package com.zaitsev.liberink.ui.screens

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor
import com.zaitsev.liberink.ui.Screen

@Composable
fun AuthorizationScreen(navController: NavController) {

    var googleAccountName by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme = LiberInkTheme.colors
    val auth = remember { FirebaseAuth.getInstance() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.paperMain),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader()

        // НОВА ЛОГІКА: Відображення акаунта після вибору
        if (googleAccountName != null) {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = theme.mainInk.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logged in as: $googleAccountName",
                        color = theme.mainInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Welcome to service",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = theme.mainInk
            )
            Text(
                text = "Sign in to continue your story",
                fontSize = 16.sp,
                color = theme.secondaryInk
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .drawBehind {
                    val shadowColor = theme.shadow
                    val shadowBlur = 30.dp.toPx()
                    val offsetY = 10.dp.toPx()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = AndroidColor.TRANSPARENT
                            setShadowLayer(shadowBlur, 0f, offsetY, shadowColor.toArgb())
                        }
                        drawRoundRect(
                            0f, 0f, size.width, size.height,
                            28.dp.toPx(), 28.dp.toPx(),
                            paint
                        )
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

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.6f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = "Email *") },
                    placeholder = { Text(text = "your@email.com", color = DarkWine20) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.mainInk,
                        unfocusedBorderColor = theme.dividerStrong
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    placeholder = { Text(text = "••••••••", color = DarkWine20) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.mainInk,
                        unfocusedBorderColor = theme.dividerStrong
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnSuccessListener {
                                    isLoading = false

                                    navController.navigate(Screen.Home.route) { // Використовуємо новий маршрут "home"
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .drawBehind {
                            val shadowColor = Color.Black.copy(alpha = 0.4f)
                            val shadowBlur = 15.dp.toPx()
                            val offsetY = 5.dp.toPx()

                            drawContext.canvas.nativeCanvas.apply {
                                val paint = Paint().apply {
                                    color = AndroidColor.TRANSPARENT
                                    setShadowLayer(shadowBlur, 0f, offsetY, shadowColor.toArgb())
                                }
                                drawRoundRect(0f, 0f, size.width, size.height, 24.dp.toPx(), 24.dp.toPx(), paint)
                            }
                        }
                        .clip(RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.mainInk,
                        contentColor = theme.paperElevated,
                        disabledContainerColor = theme.mainInk.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = theme.paperElevated, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = theme.dividerStrong)
                    Text(
                        text = " Or continue with ",
                        fontSize = 14.sp,
                        color = theme.secondaryInk,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = theme.dividerStrong)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        signInWithGoogle(
                            context = context,
                            scope = scope,
                            navController = navController,
                            onLoading = { isLoading = it },
                            onResult = { name -> googleAccountName = name }
                            )
                        },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, color = DarkWine50)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Sign in with Google", fontSize = 16.sp, color = DarkWine, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("registration") },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(text = "Don't have an account? ", color = Color.Gray, fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Sign up",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = theme.mainInk),
                fontSize = 18.sp,
                modifier = Modifier.drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height + 4.dp.toPx()
                    drawLine(color = theme.mainInk, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = strokeWidth)
                }
            )
        }
    }
}

fun signInWithGoogle(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    navController: NavController,
    onLoading: (Boolean) -> Unit,
    onResult: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)
    val auth = FirebaseAuth.getInstance()
    val webClientId = "74885674202-j2k0f2ijc8crcghkfibpb7jlrc1bs58a.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            onLoading(true)

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener { authResult ->
                            val name = authResult.user?.displayName ?: "User"
                            onResult(name) // Передаємо ім'я в UI для відображення

                            onLoading(false)

                            scope.launch {
                                kotlinx.coroutines.delay(3000)
                                navController.navigate(Screen.Home.route) { // ТЕПЕР ПРАВИЛЬНО
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("AuthError", "Firebase Error: ${e.message}")
                            Toast.makeText(context, "Firebase Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }


                } catch (e: Exception) {
                    android.util.Log.e("AuthError", "Помилка розпаковки токена", e)
                }

            } else {
                android.util.Log.e("AuthError", "Неочікуваний тип: ${credential.type}")
            }

        } catch (e: Exception) {
            android.util.Log.e("AuthError", "Google Selection Failed", e)
            if (e !is GetCredentialCancellationException) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}