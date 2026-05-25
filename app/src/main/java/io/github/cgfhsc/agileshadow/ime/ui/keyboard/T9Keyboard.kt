package io.github.cgfhsc.agileshadow.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun T9Keyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(MAIN_HEIGHT),
            horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                if (state.pinyins.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.height(MAIN_HEIGHT),
                        verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        items(count = state.pinyins.size, key = { "pinyin-$it" }) { index ->
                            GlassKeyButton(
                        isDark = isDark,
                        label = state.pinyins[index],
                                onClick = { viewModel.onAction(KeyboardAction.SelectPinyin(index)) },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                            )
                        }
                    }
                } else {
                    T9Layout.leftPunctuation.forEach { key ->
                        GlassKeyButton(
                        isDark = isDark,
                        label = key.label,
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            height = 0.dp,
                            keyBackgroundColor = TOOLBAR_BG,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(4f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                T9Layout.center9Keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(CENTER_KEY_HEIGHT),
                        horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
                    ) {
                        row.forEach { key ->
                            if (key.popupItems.isNotEmpty()) {
                                T9PopupKeyButton(
                                    isDark = isDark,
                                    label = key.label,
                                    subLabel = key.subLabel.ifEmpty { null },
                                    popupItems = key.popupItems,
                                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                                    onPopupSelect = { viewModel.onAction(KeyboardAction.DirectCommit(it)) },
                                    modifier = Modifier.weight(1f),
                                    height = CENTER_KEY_HEIGHT,
                                )
                            } else {
                                GlassKeyButton(
                                    isDark = isDark,
                                    label = key.label,
                                    subLabel = key.subLabel.ifEmpty { null },
                                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                                    modifier = Modifier.weight(1f),
                                    height = CENTER_KEY_HEIGHT,
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                T9Layout.rightFunctions.forEach { key ->
                    val icon = when (key.code) {
                        KEYCODE_DELETE -> Icons.Outlined.Backspace
                        else -> null
                    }
                    GlassKeyButton(
                        isDark = isDark,
                        label = if (icon != null) "" else key.label,
                        icon = icon,
                        subLabel = key.subLabel.ifEmpty { null },
                        repeatable = key.code == KEYCODE_DELETE,
                        onClick = {
                            when (key.code) {
                                KEYCODE_CLEAR -> viewModel.onAction(KeyboardAction.ClearComposition)
                                else -> viewModel.onAction(KeyboardAction.KeyPress(key.code))
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassKeyButton(
                        isDark = isDark,
                        label = "符号",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_SYMBOL)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
                keyBackgroundColor = TOOLBAR_BG,
            )
            Row(
                modifier = Modifier.weight(4f),
                horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                GlassKeyButton(
                        isDark = isDark,
                        label = "123",
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_NUMBER)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
                GlassKeyButton(
                        isDark = isDark,
                        label = "",
                    icon = Icons.Outlined.SpaceBar,
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(' '.code)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
                val langIcon = Icons.Outlined.Language
                GlassKeyButton(
                        isDark = isDark,
                        label = "",
                    icon = langIcon,
                    subLabel = if (state.activeKeyboard == KeyboardType.ENGLISH) "EN" else "中",
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_SWITCH_LANG)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
            }
            GlassKeyButton(
                        isDark = isDark,
                        label = "确认",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_ENTER)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
                keyBackgroundColor = CONFIRM_BG,
                textColor = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(KEYBOARD_BOTTOM_SPACER))
    }
}
