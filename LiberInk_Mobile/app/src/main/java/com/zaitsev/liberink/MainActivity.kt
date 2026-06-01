package com.zaitsev.liberink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.zaitsev.liberink.data.RetrofitClient
import com.zaitsev.liberink.ui.Screen
import com.zaitsev.liberink.ui.components.AppBottomBar
import com.zaitsev.liberink.ui.screens.AuthorizationScreen
import com.zaitsev.liberink.ui.screens.CreateLoreItemScreen
import com.zaitsev.liberink.ui.screens.CreateNoteScreen
import com.zaitsev.liberink.ui.screens.HomeScreen
import com.zaitsev.liberink.ui.screens.LoreItemDetailScreen
import com.zaitsev.liberink.ui.screens.LoreScreen
import com.zaitsev.liberink.ui.screens.NotesScreen
import com.zaitsev.liberink.ui.screens.OnboardingNoteScreen
import com.zaitsev.liberink.ui.screens.OnboardingScreen
import com.zaitsev.liberink.ui.screens.ProfileScreen
import com.zaitsev.liberink.ui.screens.ReaderScreen
import com.zaitsev.liberink.ui.screens.RegistrationScreen
import com.zaitsev.liberink.ui.screens.WorldDetailScreen
import com.zaitsev.liberink.ui.theme.LiberInkTheme
import com.zaitsev.liberink.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

                    // Приховуємо нижню панель для екранів читання, нотаток та деталей лору
                    val shouldShowBottomBar = currentRoute != null &&
                            currentRoute !in screensWithoutBottomBar &&
                            !currentRoute.startsWith("create_note") &&
                            !currentRoute.startsWith("registration") &&
                            !currentRoute.startsWith("reader_screen") &&
                            !currentRoute.startsWith("create_lore") &&
                            !currentRoute.startsWith("lore_detail")

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

                                val title = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (_: Exception) { rawTitle }
                                val content = try { URLDecoder.decode(rawContent, "UTF-8") } catch (_: Exception) { rawContent }

                                ReaderScreen(
                                    navController = navController,
                                    bookId = bookId,
                                    title = title,
                                    content = content
                                )
                            }

                            composable(Screen.Lore.route) {
                                LoreScreen(navController)
                            }

                            composable("world_detail/{worldName}") { backStackEntry ->
                                val worldName = backStackEntry.arguments?.getString("worldName")
                                WorldDetailScreen(navController, worldName)
                            }

                            // Нові маршрути для створення елементів лору
                            composable("create_lore/{type}") { backStackEntry ->
                                val type = backStackEntry.arguments?.getString("type") ?: "Locations"
                                CreateLoreItemScreen(navController, type)
                            }

                            // Новий маршрут для детального перегляду персонажа/локації
                            composable("lore_detail/{itemId}") { backStackEntry ->
                                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                                LoreItemDetailScreen(navController, itemId)
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

                                val title = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (_: Exception) { rawTitle }
                                val content = try { URLDecoder.decode(rawContent, "UTF-8") } catch (_: Exception) { rawContent }

                                CreateNoteScreen(
                                    navController = navController,
                                    noteId = id,
                                    initialTitle = title,
                                    initialContent = content
                                )
                            }

                            composable(Screen.Profile.route) { ProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}
