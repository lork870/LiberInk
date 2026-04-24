package com.zaitsev.liberink.ui.theme

import androidx.compose.ui.graphics.Color

// --- PRIMARY AND DEEP COLORS ---
val DarkWine = Color(0xFF4A0404)       // 4A0404
val DeepCoffee = Color(0xFF54362E)     // 54362E
val RichBlack = Color(0xFF170505)      // 170505
val Charcoal = Color(0xFF1A1A1A)       // 1A1A1A
val PureBlack = Color(0xFF000000)      // 000000
val Gray66 = Color(0xFF666666)         // 666666

// --- LIGHT AND PAPER COLORS ---
val PureWhite = Color(0xFFFFFFFF)      // FFFFFF
val PaperOffWhite = Color(0xFFFFFFFD)  // FFFFFD
val AntiqueCream = Color(0xFFF5F2E9)   // F5F2E9
val WarmSand = Color(0xFFF9F3E3)       // F9F3E3
val SoftGrayPaper = Color(0xFFF5F5F5)  // F5F5F5
val MilkyWhite = Color(0xFFFFFCF5)     // FFFCF5
val LightBred = Color(0xFFF0E7E4)      // F0E7E4

// --- ACCENTS AND WARM TONES ---
val AmberGold = Color(0xFFF4A300)      // F4A300
val PaleGold = Color(0xFFFFE1A4)       // FFE1A4
val Terracotta = Color(0xFFD88460)     // D88460
val MutedRose = Color(0xFFA48180)      // A48180
val PeachCream = Color(0xFFFBE0C5)     // FBE0C5
val RoseTaupe = Color(0xFFEDD4C8)      // EDD4C8
val MutedDust = Color(0xFFE9CABD)      // E9CABD

// --- TRANSPARENT VARIATIONS (ALPHA) ---
// DarkWine Transparency
val DarkWine50 = DarkWine.copy(alpha = 0.5f)
val DarkWine20 = DarkWine.copy(alpha = 0.2f)
val DarkWine10 = DarkWine.copy(alpha = 0.1f)

// Black Transparency
val Black70 = PureBlack.copy(alpha = 0.7f)   // 1A1A1A (70%)
val Black60 = PureBlack.copy(alpha = 0.6f)   // 1A1A1A (60%)
val Black50 = PureBlack.copy(alpha = 0.5f)   // 000000 (50%)
val Black40 = PureBlack.copy(alpha = 0.4f)   // 000000 (40%)
val Black25 = PureBlack.copy(alpha = 0.25f)  // 000000 (25%)

// White Transparency
val White50 = PureWhite.copy(alpha = 0.5f)   // FFFFFF (50%)
val White20 = PureWhite.copy(alpha = 0.2f)   // FFFFFF (20%)
val White15 = PureWhite.copy(alpha = 0.15f)  // FFFFFF (15%)

// Accent Transparency
val AmberGold20 = AmberGold.copy(alpha = 0.2f)   // E29700 (20%) / F4A300
val Terracotta30 = Terracotta.copy(alpha = 0.3f) // D88460 (30%)
val Terracotta10 = Terracotta.copy(alpha = 0.1f) // D88460 (10%)
val PaleGold25 = PaleGold.copy(alpha = 0.25f)    // FFE1A4 (25%)
val PaleGold20 = PaleGold.copy(alpha = 0.20f)    // FFE1A4 (20%)

// Specific Mixes
val AntiqueCream90 = AntiqueCream.copy(alpha = 0.9f) // F5F2E9 (90%)