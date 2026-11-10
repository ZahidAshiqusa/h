package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val UltraCyberColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = ObsidianNavy,
    primaryContainer = GlassCardHighlight,
    onPrimaryContainer = ElectricCyan,
    secondary = NeonViolet,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF231845),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = StatusConnected,
    onTertiary = ObsidianNavy,
    background = ObsidianNavy,
    onBackground = TextPrimary,
    surface = DeepSpaceBlue,
    onSurface = TextPrimary,
    surfaceVariant = GlassCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderCyan,
    error = StatusError,
    onError = TextPrimary
)

val UltraShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    accentCyan: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = if (accentCyan) {
        UltraCyberColorScheme
    } else {
        UltraCyberColorScheme.copy(
            primary = NeonViolet,
            onPrimary = TextPrimary,
            secondary = ElectricCyan
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = UltraShapes,
        content = content
    )
}
