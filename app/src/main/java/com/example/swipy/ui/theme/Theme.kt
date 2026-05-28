package com.example.swipy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SwipyTheme(accentIndex: Int = 0, content: @Composable () -> Unit) {
    val primaryAccent = when (accentIndex) {
        1 -> SoftPink
        2 -> SageGreen
        else -> DustyBlue
    }

    val SwipyLightColors = lightColorScheme(
        primary          = primaryAccent,
        onPrimary        = Color.White,
        primaryContainer = primaryAccent.copy(alpha = 0.15f),
        secondary        = SoftPink,
        onSecondary      = Color.White,
        tertiary         = SageGreen,
        onTertiary       = Color.White,
        background       = WarmWhite,
        onBackground     = Color(0xFF2C2C2C),
        surface          = Color.White,
        onSurface        = Color(0xFF2C2C2C),
        surfaceVariant   = Color(0xFFF0EDE8),
        outline          = LightGray
    )

    MaterialTheme(
        colorScheme = SwipyLightColors,
        typography  = SwipyTypography,
        shapes      = SwipyShapes,
        content     = content
    )
}
