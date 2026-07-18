package com.listingstudio.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF6C4DFF)

private val Light = lightColorScheme(
    primary = Accent,
    secondary = Color(0xFF00C2A8)
)
private val Dark = darkColorScheme(
    primary = Accent,
    secondary = Color(0xFF00C2A8)
)

@Composable
fun ListingStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        content = content
    )
}
