package com.memeitizer.appstore.ui
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val NeonDark = darkColorScheme(
    primary = Color(0xFF00FFA8),
    secondary = Color(0xFFFF2EB6),
    background = Color(0xFF0A0A0F),
    surface = Color(0xFF101018),
    onPrimary = Color.Black, onSecondary = Color.Black,
    onBackground = Color(0xFFEAEAF5), onSurface = Color(0xFFEAEAF5)
)
private val NeonLight = lightColorScheme(
    primary = Color(0xFF00C98E),
    secondary = Color(0xFFE61E9B),
    background = Color(0xFFF8F9FC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.Black, onSecondary = Color.White,
    onBackground = Color(0xFF111118), onSurface = Color(0xFF1B1B22)
)
@Composable fun NeoTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) NeonDark else NeonLight, content = content)
}
