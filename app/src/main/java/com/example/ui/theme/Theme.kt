package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TacticalCyan,
    onPrimary = SpaceBlack,
    primaryContainer = SpaceCardBg,
    onPrimaryContainer = TacticalCyan,
    secondary = StellarGold,
    onSecondary = SpaceBlack,
    secondaryContainer = SpaceDark,
    onSecondaryContainer = StellarGold,
    tertiary = NebulaPurple,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SpaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SpaceCardBorder,
    error = LaserRed
)

@Composable
fun NebulaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
