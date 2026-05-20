package dev.yeying.ime.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import dev.yeying.ime.ui.theme.GlassParams
import dev.yeying.ime.ui.theme.liquidGlass

@Composable
fun GlassKeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    icon: ImageVector? = null,
    width: Dp = 0.dp,
    height: Dp = 44.dp,
    isActive: Boolean = false,
    isDark: Boolean = false,
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(8.dp)

    val pressedAlpha = if (isPressed) 0.30f else 0.45f
    val activeAlpha = if (isActive) 0.38f else pressedAlpha

    Box(
        modifier = modifier
            .then(if (height > 0.dp) Modifier.height(height) else Modifier)
            .then(if (width > 0.dp) Modifier.width(width) else Modifier)
            .clip(shape)
            .liquidGlass(
                cornerRadius = 8.dp,
                isDark = isDark,
                params = GlassParams(
                    baseAlpha = activeAlpha,
                    borderAlpha = 0.20f,
                    keyBackground = if (isDark) Color(0xFF4A4A4A) else Color(0xFFF8F8F8),
                ),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                )
                if (subLabel != null) {
                    Text(text = subLabel, fontSize = 12.sp)
                }
            }
        } else if (subLabel != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = label, fontSize = 16.sp)
                Text(text = subLabel, fontSize = 10.sp)
            }
        } else {
            Text(
                text = label,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun KeyboardBottomBar(
    onClipboardClick: () -> Unit = {},
    isInClipboard: Boolean = false,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(28.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
        ) {
            Spacer(modifier = Modifier.weight(5f))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isInClipboard) Icons.AutoMirrored.Outlined.ArrowBack else Icons.Outlined.ContentPaste,
                    contentDescription = if (isInClipboard) "返回" else "剪贴板",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = if (isInClipboard) onBack else onClipboardClick),
                )
            }
        }
    }
}
