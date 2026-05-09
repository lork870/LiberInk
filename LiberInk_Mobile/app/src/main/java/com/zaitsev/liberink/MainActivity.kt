package com.zaitsev.liberink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.zaitsev.liberink.ui.theme.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import com.zaitsev.liberink.data.RetrofitClient
import kotlinx.coroutines.launch
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Ініціалізуємо тему (завантажуємо збережену з SharedPreferences)
        ThemeManager.initialize(this)

        val auth = FirebaseAuth.getInstance()

        // 2. Логіка перевірки Firebase
        lifecycleScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val uid = user.uid
                try {
                    val books = RetrofitClient.instance.getBooks(uid)
                    android.util.Log.d("LIBERINK_DEBUG", "УСПІХ! Книг у базі: ${books.size}")
                } catch (e: Exception) {
                    android.util.Log.e("LIBERINK_DEBUG", "ПОМИЛКА МЕРЕЖІ: ${e.localizedMessage}")
                }
            }
        }

        setContent {
            LiberInkTheme(darkTheme = ThemeManager.isDarkTheme) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LiberInkTheme.colors.paperMain
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val screensWithoutBottomBar = listOf(
                        "onboarding", "authorization", "onboardingNoteScreen"
                    )

                    val shouldShowBottomBar = currentRoute != null &&
                            currentRoute !in screensWithoutBottomBar &&
                            !currentRoute.startsWith("create_note") &&
                            !currentRoute.startsWith("registration") &&
                            !currentRoute.startsWith("reader_screen")


                    Scaffold(
                        containerColor = LiberInkTheme.colors.paperMain,
                        bottomBar = {
                            if (shouldShowBottomBar) {
                                AppBottomBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route, navOptions ->
                                        navController.navigate(route, navOptions)
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
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

                            composable(
                                route = "reader_screen?bookId={bookId}&title={title}&content={content}",
                                arguments = listOf(
                                    navArgument("bookId") { type = NavType.StringType; nullable = true },
                                    navArgument("title") { type = NavType.StringType; nullable = true },
                                    navArgument("content") { type = NavType.StringType; nullable = true }
                                )
                            ) { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                                val rawTitle = backStackEntry.arguments?.getString("title") ?: "Untitled"
                                val rawContent = backStackEntry.arguments?.getString("content") ?: ""

                                val title = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
                                val content = try { URLDecoder.decode(rawContent, "UTF-8") } catch (e: Exception) { rawContent }

                                ReaderScreen(
                                    navController = navController,
                                    bookId = bookId,
                                    title = title,
                                    content = content
                                )
                            }

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

                                val title = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
                                val content = try { URLDecoder.decode(rawContent, "UTF-8") } catch (e: Exception) { rawContent }

                                CreateNoteScreen(
                                    navController = navController,
                                    noteId = id,
                                    initialTitle = title,
                                    initialContent = content
                                )
                            }

                            composable(Screen.Lore.route) { PlaceholderScreen("Lore & Worldbuilding") }
                            composable(Screen.Profile.route) { ProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String, isLoading: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LiberInkTheme.colors.paperMain
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                color = LiberInkTheme.colors.mainInk,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isLoading) "Loading data..." else "is under development",
                color = LiberInkTheme.colors.secondaryInk,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = LiberInkTheme.colors.accentGold,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}