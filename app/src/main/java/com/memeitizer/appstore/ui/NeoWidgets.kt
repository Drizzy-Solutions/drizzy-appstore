package com.memeitizer.appstore.ui
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable fun NeonGradientBackdrop() {
    val t by rememberInfiniteTransition().animateFloat(0f,1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)))
    val colors = listOf(Color(0x3300FFA8), Color(0x33000000), Color(0x33FF2EB6))
    Box(Modifier.fillMaxSize()
        .background(Brush.linearGradient(colors,
            start = androidx.compose.ui.geometry.Offset(0f, 0f + 500f * t),
            end = androidx.compose.ui.geometry.Offset(1200f * (1f - t), 1600f)))
        .blur(40.dp))
}
@Composable fun GlassCard(modifier: Modifier = Modifier, glow: Color, corner: Dp = 22.dp, content: @Composable ColumnScope.() -> Unit) {
    val scale by rememberInfiniteTransition().animateFloat(1f,1.01f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse))
    Box(modifier = modifier.scale(scale)
        .clip(RoundedCornerShape(corner))
        .background(Color(0x66FFFFFF))
        .border(1.dp, glow.copy(alpha = .35f), RoundedCornerShape(corner))
    ) { Column(Modifier.padding(14.dp), content = content) }
}
@Composable fun StatusPill(text: String, color: Color) {
    Surface(color = color.copy(alpha = .18f), contentColor = color, shape = RoundedCornerShape(999.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}
@Composable fun PrimaryGlowButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled) { Text(label) }
}
