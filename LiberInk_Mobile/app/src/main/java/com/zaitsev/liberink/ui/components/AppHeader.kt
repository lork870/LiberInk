package com.zaitsev.liberink.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaitsev.liberink.R
import com.zaitsev.liberink.ui.theme.LiberInkTheme

@Composable
fun AppHeader(
    modifier: Modifier = Modifier
) {
    val theme = LiberInkTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 16.dp)
            .height(80.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_liberink),
            contentDescription = "Logo",
            modifier = Modifier
                .size(70.dp)
                .padding(end = 12.dp)
        )

        Text(
            text = "LiberInk",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = theme.mainInk
        )
    }
}