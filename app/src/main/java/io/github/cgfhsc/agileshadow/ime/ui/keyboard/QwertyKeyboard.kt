package io.github.cgfhsc.agileshadow.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.outlined.KeyboardCapslock
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NUMBER_ROW = listOf("1","2","3","4","5","6","7","8","9","0")

private val FUNCTION_CODES = setOf(
    KEYCODE_SHIFT, KEYCODE_DELETE, KEYCODE_ENTER, KEYCODE_SWITCH_LANG,
    KEYCODE_SYMBOL, KEYCODE_NUMBER, KEYCODE_SPACE,
)

@Composable
fun QwertyKeyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    val activeKeyboard by viewModel.activeKeyboardFlow.collectAsState()
    val capsState by viewModel.capsStateFlow.collectAsState()
    val isEnglish = activeKeyboard == KeyboardType.ENGLISH

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NUMBER_ROW.forEach { num ->
                GlassKeyButton(
                        isDark = isDark,
                        label = num,
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(num[0].code)) },
                    modifier = Modifier.weight(1f),
                    height = 38.dp,
                )
            }
        }

        QwertyLayout.rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { key ->
                    val isPeriod = key.label == "。" || key.label == "."
                    val isComma = key.label == "," || key.label == "，"
                    val displayLabel = when {
                        isPeriod && isEnglish -> "."
                        isPeriod && !isEnglish -> "。"
                        isComma && isEnglish -> ","
                        isComma && !isEnglish -> "，"
                        capsState != CapsState.NONE && key.label.length == 1 && key.label[0].isLetter() -> key.label.uppercase()
                        else -> key.label
                    }
                    val displayCode = when {
                        isPeriod && isEnglish -> '.'.code
                        isPeriod && !isEnglish -> '。'.code
                        isComma && isEnglish -> ','.code
                        isComma && !isEnglish -> '，'.code
                        else -> key.code
                    }

                    val label: String
                    val icon = when (displayCode) {
                        KEYCODE_SHIFT -> { label = ""; Icons.Outlined.KeyboardCapslock }
                        KEYCODE_DELETE -> { label = ""; Icons.AutoMirrored.Outlined.Backspace }
                        KEYCODE_ENTER -> { label = ""; Icons.AutoMirrored.Outlined.KeyboardReturn }
                        KEYCODE_SWITCH_LANG -> {
                            label = ""
                            Icons.Outlined.Language
                        }
                        KEYCODE_SPACE -> { label = ""; Icons.Outlined.SpaceBar }
                        else -> { label = displayLabel; null }
                    }
                    val subLabel = when (displayCode) {
                        KEYCODE_SWITCH_LANG -> if (isEnglish) "EN" else "中"
                        else -> null
                    }
                    val isToolbar = displayCode in FUNCTION_CODES && displayCode != KEYCODE_SPACE || isPeriod || key.label == ","
                    val isConfirm = displayCode == KEYCODE_ENTER
                    val isLetterKey = icon == null && displayCode !in FUNCTION_CODES && !isPeriod && key.label != ","

                    if (isLetterKey) {
                        QwertyKeyButton(
                            isDark = isDark,
                            label = label,
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(displayCode)) },
                            modifier = if (key.width == 0.dp) Modifier.weight(1f) else Modifier,
                            height = 42.dp,
                        )
                    } else {
                        GlassKeyButton(
                            isDark = isDark,
                            label = label,
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(displayCode)) },
                            icon = icon,
                            subLabel = subLabel,
                            width = key.width,
                            modifier = if (key.width == 0.dp) Modifier.weight(1f) else Modifier,
                            height = 42.dp,
                            isActive = displayCode == KEYCODE_SHIFT && capsState != CapsState.NONE,
                            repeatable = displayCode == KEYCODE_DELETE,
                            keyBackgroundColor = when {
                                isConfirm -> CONFIRM_BG
                                isToolbar -> TOOLBAR_BG
                                else -> null
                            },
                            textColor = if (isConfirm) Color.White else DEFAULT_TEXT,
                        )
                    }
                }
            }
        }

        KeyboardBottomSpacer()
    }
}
