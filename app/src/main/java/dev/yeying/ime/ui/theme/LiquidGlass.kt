package dev.yeying.ime.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GlassParams(
    val baseAlpha: Float = 0.90f,
    val glassHighAlpha: Float = 0.30f,
    val glassLowAlpha: Float = 0.10f,
    val specularAlpha: Float = 0.45f,
    val innerGlowAlpha: Float = 0.08f,
    val borderAlpha: Float = 0.08f,
)

fun Modifier.liquidGlass(
    cornerRadius: Dp = 16.dp,
    isDark: Boolean = false,
    params: GlassParams = if (isDark) GlassParams(
        glassHighAlpha = 0.12f,
        glassLowAlpha = 0.04f,
        specularAlpha = 0.18f,
        innerGlowAlpha = 0.03f,
    ) else GlassParams(),
): Modifier = this.drawBehind {
    val r = CornerRadius(cornerRadius.toPx())
    val w = size.width
    val h = size.height

    // 1. 底色（高 alpha，遮住底层）
    drawRoundRect(
        color = Color.White.copy(alpha = params.baseAlpha),
        cornerRadius = r,
    )

    // 2. 主体渐变
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = params.glassHighAlpha),
                Color.White.copy(alpha = params.glassLowAlpha),
            )
        ),
        cornerRadius = r,
    )

    // 3. 高光条（顶部）
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                Color.White.copy(alpha = params.specularAlpha),
                Color.Transparent,
            ),
            startX = w * 0.15f,
            endX = w * 0.85f,
        ),
        topLeft = Offset(w * 0.15f, 3.dp.toPx()),
        size = Size(w * 0.7f, 1.5.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx()),
    )

    // 4. 内发光（底部）
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = params.innerGlowAlpha))
        ),
        topLeft = Offset(4.dp.toPx(), h - 8.dp.toPx()),
        size = Size(w - 8.dp.toPx(), 4.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx()),
    )

    // 5. 边框（仅深色主题）
    if (isDark && params.borderAlpha > 0f) {
        drawRoundRect(
            color = Color.White.copy(alpha = params.borderAlpha),
            cornerRadius = r,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
