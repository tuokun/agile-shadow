package io.github.cgfhsc.agileshadow.ime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3C3C43),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainer = Color.White.copy(alpha = 0.90f),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9E9EA3),
    surface = Color(0xFF2B2B2B),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainer = Color.Black.copy(alpha = 0.90f),
)

@Composable
fun AgileShadowTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        content = content,
    )
}
