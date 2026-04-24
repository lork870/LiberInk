package com.zaitsev.liberink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zaitsev.liberink.ui.Screen
import com.zaitsev.liberink.ui.components.AppBottomBar
import com.zaitsev.liberink.ui.screens.*
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import com.zaitsev.liberink.data.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            android.util.Log.d("LIBERINK_DEBUG", "=== ТЕСТ ЗАПУЩЕНО ===")

            // Почекаємо 2 секунди, щоб все ініціалізувалося
            kotlinx.coroutines.delay(2000)

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val uid = user.uid
                android.util.Log.d("LIBERINK_DEBUG", "Знайдено UID: $uid")

                try {
                    val books = RetrofitClient.instance.getBooks(uid)
                    android.util.Log.d("LIBERINK_DEBUG", "УСПІХ! Книг у базі: ${books.size}")
                } catch (e: Exception) {
                    android.util.Log.e("LIBERINK_DEBUG", "ПОМИЛКА МЕРЕЖІ: ${e.localizedMessage}")
                }
            } else {
                android.util.Log.d("LIBERINK_DEBUG", "Користувач ще не залогінився")
            }
        }

        val auth = FirebaseAuth.getInstance()

        setContent {
            LiberInkTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val screensWithoutBottomBar = listOf(
                    "onboarding", "authorization", "registration", "onboardingNoteScreen"
                )

                val shouldShowBottomBar = currentRoute !in screensWithoutBottomBar &&
                        currentRoute?.startsWith("create_note") == false &&
                        currentRoute != null

                Scaffold(
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            AppBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        // ФІКС: Завжди стартуємо з onboarding, але... (див. нижче)
                        startDestination = "onboarding",
                        modifier = Modifier.padding(
                            PaddingValues(
                                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                                top = 0.dp,
                                bottom = if (shouldShowBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                            )
                        )
                    ) {
                        composable("onboarding") {
                            // ...додаємо логіку перевірки прямо тут
                            if (auth.currentUser != null) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            } else {
                                OnboardingScreen(navController)
                            }
                        }

                        composable("authorization") { AuthorizationScreen(navController) }
                        composable("onboardingNoteScreen") { OnboardingNoteScreen(navController) }

                        composable(
                            route = "registration?noteId={noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.StringType; nullable = true })
                        ) { backStackEntry ->
                            RegistrationScreen(navController, backStackEntry.arguments?.getString("noteId"))
                        }

                        composable(Screen.Home.route) { HomeScreen(navController) }
                        composable(Screen.Notes.route) { NotesScreen(navController) }

                        composable(
                            route = "create_note?id={id}&title={title}&content={content}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType; nullable = true },
                                navArgument("title") { type = NavType.StringType; nullable = true },
                                navArgument("content") { type = NavType.StringType; nullable = true }
                            )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")
                            val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
                            val rawContent = backStackEntry.arguments?.getString("content") ?: ""

                            // Безпечне розкодування
                            val title = try { java.net.URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
                            val content = try { java.net.URLDecoder.decode(rawContent, "UTF-8") } catch (e: Exception) { rawContent }

                            CreateNoteScreen(
                                navController = navController,
                                noteId = id,
                                initialTitle = title,
                                initialContent = content
                            )
                        }

                        composable(Screen.Lore.route) { PlaceholderScreen("Lore & Worldbuilding") }
                        composable(Screen.Profile.route) { PlaceholderScreen("User Profile") }
                    }
                }
            }
        }
    }
}
@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F3E9)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name is under development",
            color = Color.Gray,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}