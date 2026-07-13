package com.quickthought.skillvault.ui.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Footer() {
    val gradientColors = listOf(
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFFFC107), // Amber
        Color(0xFF4CAF50)  // Green
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Made with love in India",
            style = TextStyle(
                brush = Brush.linearGradient(colors = gradientColors),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Cursive
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "🇮🇳 ❤️",
            fontSize = 44.sp,
            style = TextStyle(textAlign = TextAlign.Center)
        )
    }
}