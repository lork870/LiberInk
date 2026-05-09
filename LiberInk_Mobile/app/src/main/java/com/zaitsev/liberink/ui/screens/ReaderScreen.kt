package com.zaitsev.liberink.ui.screens

import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    bookId: String,
    title: String,
    content: String
) {
    val theme = LiberInkTheme.colors

    Scaffold(
        containerColor = theme.paperMain,
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = theme.mainInk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = theme.mainInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.paperMain)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                // Скрол залишаємо, якщо тексту дуже багато
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ПРОФЕСІЙНИЙ РЕНДЕР HTML
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    TextView(context).apply {
                        // Налаштування вигляду TextView, щоб відповідав темі
                        textSize = 18f
                        setTextColor(android.graphics.Color.parseColor("#" + Integer.toHexString(theme.mainInk.toArgb()).substring(2)))
                        // Якщо потрібно змінити шрифт на кастомний (наприклад, з ресурсів)
                        // typeface = ResourcesCompat.getFont(context, R.font.your_font)
                    }
                },
                update = { textView ->
                    textView.text = HtmlCompat.fromHtml(
                        content,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}