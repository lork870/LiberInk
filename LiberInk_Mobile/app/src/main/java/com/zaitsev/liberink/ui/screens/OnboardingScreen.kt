package com.zaitsev.liberink.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zaitsev.liberink.R
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import com.zaitsev.liberink.ui.components.AppHeader
import android.graphics.Color as AndroidColor
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val theme = LiberInkTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.paperMain), // Використовуємо колір з теми
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.wrapContentHeight(),
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    imageRes = R.drawable.onboarding_image_1,
                    title = "Your story,\nYour Sanctuary",
                    description = "LiberInk is a distraction-free writing environment that helps you focus on what matters—your words. Craft, revise, and perfect your stories with powerful version control"
                )

                1 -> OnboardingPage(
                    imageRes = R.drawable.onboarding_image_2,
                    title = "Write Boundlessly,\nEverywhere",
                    description = "Start writing over morning coffee on your phone and continue in the evening on your laptop. Your work syncs instantly and securely across all your devices"
                )

                2 -> OnboardingPage(
                    imageRes = R.drawable.onboarding_image_3,
                    title = "Control\nEvery Idea",
                    description = "Don't be afraid to experiment. Our system automatically saves all your changes, allowing you to easily compare drafts and return to previous ideas"
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val dotColor =
                    if (pagerState.currentPage == iteration) theme.mainInk else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("onboardingNoteScreen") },
                colors = ButtonDefaults.buttonColors(containerColor = theme.mainInk),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .drawBehind {
                        val shadowColor = Color.Black.copy(alpha = 0.6f)
                        val shadowBlur = 15.dp.toPx()
                        val offsetY = 5.dp.toPx()

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = Paint().apply {
                                color = AndroidColor.TRANSPARENT
                                setShadowLayer(
                                    shadowBlur,
                                    0f,
                                    offsetY,
                                    shadowColor.toArgb()
                                )
                            }
                            drawRoundRect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                                24.dp.toPx(),
                                24.dp.toPx(),
                                paint
                            )
                        }
                    }
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Text(
                    text = "Start Your Story Now",
                    color = Color.White,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            TextButton(
                onClick = { navController.navigate("authorization") },
                modifier = Modifier.padding(top = 12.dp)
            ) {

                Text(
                    text = "Already have an account? ",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp
                )
                Text(
                    text = "Sign in",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = theme.mainInk,
                    ),
                    fontSize = 18.sp,
                    modifier = Modifier.drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height + 4.dp.toPx()
                        drawLine(
                            color = theme.mainInk,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(imageRes: Int, title: String, description: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 40.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color(0xFF4A0E0E),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .drawBehind {
                        val shadowColor = Color.Black.copy(alpha = 0.6f)
                        val shadowBlur = 30.dp.toPx()
                        val offsetY = 10.dp.toPx()

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = Paint().apply {
                                color = AndroidColor.TRANSPARENT
                                setShadowLayer(
                                    shadowBlur,
                                    0f,
                                    offsetY,
                                    shadowColor.toArgb()
                                )
                            }
                            drawRoundRect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                                24.dp.toPx(),
                                24.dp.toPx(),
                                paint
                            )
                        }
                    }
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .heightIn(min = 90.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = description,
                fontSize = 15.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}