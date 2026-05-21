package dev.yeying.ime.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

val TOOLBAR_BG = Color(0xFFDDE2E8)
val CONFIRM_BG = Color(0xFF3482FF)
val KEYBOARD_BG = Color(0xFFEEF0F3)
val CANDIDATE_BG = Color.Transparent
val DEFAULT_TEXT = Color(0xFF333333)

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
    keyBackgroundColor: Color? = null,
    textColor: Color = DEFAULT_TEXT,
    showBorder: Boolean = true,
    iconSize: Dp = 18.dp,
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(8.dp)

    val defaultBg = if (isDark) Color(0xFF4A4A4A) else Color(0xFFFFFFFF)
    val bgColor = keyBackgroundColor ?: defaultBg
    val displayBg = when {
        isPressed || isActive -> Color(bgColor.red * 0.88f, bgColor.green * 0.88f, bgColor.blue * 0.88f)
        else -> bgColor
    }
    val borderColor = if (keyBackgroundColor != null) Color(0xFFCDD2D8) else Color(0xFFE0E4E8)

    Box(
        modifier = modifier
            .then(if (height > 0.dp) Modifier.height(height) else Modifier)
            .then(if (width > 0.dp) Modifier.width(width) else Modifier)
            .clip(shape)
            .background(displayBg, shape)
            .then(if (showBorder) Modifier.border(1.dp, borderColor, shape) else Modifier)
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
                    modifier = Modifier.size(iconSize),
                    tint = textColor,
                )
                if (subLabel != null) {
                    Text(text = subLabel, fontSize = 12.sp, color = textColor)
                }
            }
        } else if (subLabel != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-2).dp),
            ) {
                Text(text = label, fontSize = 16.sp, color = textColor)
                Text(text = subLabel, fontSize = 10.sp, color = textColor)
            }
        } else {
            Text(
                text = label,
                fontSize = 18.sp,
                color = textColor,
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
