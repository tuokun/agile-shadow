package io.github.cgfhsc.agileshadow.ime.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val TOOLBAR_BG = Color(0xFFDDE2E8)
val CONFIRM_BG = Color(0xFF3482FF)
val KEYBOARD_BG = Color(0xFFEEF0F3)
val CANDIDATE_BG = Color.Transparent
val DEFAULT_TEXT = Color(0xFF333333)

val DARK_TOOLBAR_BG = Color(0xFF252628)
val DARK_CONFIRM_BG = Color(0xFF267AF7)
val DARK_KEYBOARD_BG = Color(0xFF17181A)
val DARK_KEY_BG = Color(0xFF323335)
val DARK_DEFAULT_TEXT = Color.White
val DARK_BORDER = Color(0xFF4A4A4E)

val CENTER_KEY_HEIGHT = 52.dp
val CENTER_ROW_GAP = 4.dp
val MAIN_HEIGHT = CENTER_KEY_HEIGHT * 3 + CENTER_ROW_GAP * 2
val KEYBOARD_BOTTOM_SPACER = 28.dp

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
    repeatable: Boolean = false,
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(8.dp)

    val resolvedKeyBg = when {
        isDark && keyBackgroundColor == TOOLBAR_BG -> DARK_TOOLBAR_BG
        isDark && keyBackgroundColor == CONFIRM_BG -> DARK_CONFIRM_BG
        else -> keyBackgroundColor
    }
    val defaultBg = if (isDark) DARK_KEY_BG else Color(0xFFFFFFFF)
    val bgColor = resolvedKeyBg ?: defaultBg
    val displayBg = when {
        isPressed || isActive -> Color(bgColor.red * 0.88f, bgColor.green * 0.88f, bgColor.blue * 0.88f)
        else -> bgColor
    }
    val resolvedTextColor = when {
        textColor != DEFAULT_TEXT -> textColor
        isDark -> DARK_DEFAULT_TEXT
        else -> DEFAULT_TEXT
    }
    val borderColor = if (isDark) DARK_BORDER else {
        if (keyBackgroundColor != null) Color(0xFFCDD2D8) else Color(0xFFE0E4E8)
    }

    val baseModifier = modifier
        .then(if (height > 0.dp) Modifier.height(height) else Modifier)
        .then(if (width > 0.dp) Modifier.width(width) else Modifier)
        .clip(shape)
        .background(displayBg, shape)
        .then(if (showBorder) Modifier.border(1.dp, borderColor, shape) else Modifier)

    if (repeatable) {
        val scope = rememberCoroutineScope()
        val pressed = remember { mutableStateOf(false) }

        Box(
            modifier = baseModifier.pointerInput(interactionSource) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    scope.launch { interactionSource.emit(press) }
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                    pressed.value = true

                    val repeatJob: Job = scope.launch {
                        delay(400L)
                        var interval = 120L
                        while (pressed.value) {
                            onClick()
                            delay(interval)
                            interval = maxOf(30L, interval - 10L)
                        }
                    }

                    val up = waitForUpOrCancellation()
                    pressed.value = false
                    repeatJob.cancel()
                    scope.launch {
                        interactionSource.emit(
                            if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press)
                        )
                    }
                }
            },
            contentAlignment = Alignment.Center,
        ) { KeyContent(icon, subLabel, label, resolvedTextColor, iconSize) }
    } else {
        Box(
            modifier = baseModifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                },
            ),
            contentAlignment = Alignment.Center,
        ) { KeyContent(icon, subLabel, label, resolvedTextColor, iconSize) }
    }
}

@Composable
private fun KeyContent(
    icon: ImageVector?,
    subLabel: String?,
    label: String,
    textColor: Color,
    iconSize: Dp,
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

