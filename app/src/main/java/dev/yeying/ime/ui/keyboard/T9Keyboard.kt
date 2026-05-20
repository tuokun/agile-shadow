package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun T9Keyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 主区域：三列（左标点 + 中九键 + 右功能）
        Row(
            modifier = Modifier.fillMaxWidth().height(168.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 左列：拼音组合（输入时）或标点符号（空闲时）
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (state.pinyins.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        items(count = state.pinyins.size, key = { "pinyin-$it" }) { index ->
                            GlassKeyButton(
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
                            label = key.label,
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            height = 0.dp,
                        )
                    }
                }
            }

            // 中列：九键网格
            Column(
                modifier = Modifier.weight(4f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                T9Layout.center9Keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        row.forEach { key ->
                            GlassKeyButton(
                                label = key.label,
                                subLabel = key.subLabel.ifEmpty { null },
                                onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                                modifier = Modifier.weight(1f),
                                height = 52.dp,
                            )
                        }
                    }
                }
            }

            // 右列：功能键
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                T9Layout.rightFunctions.forEach { key ->
                    val icon = when (key.code) {
                        KEYCODE_DELETE -> Icons.Outlined.Backspace
                        else -> null
                    }
                    GlassKeyButton(
                        label = if (icon != null) "" else key.label,
                        icon = icon,
                        subLabel = key.subLabel.ifEmpty { null },
                        onClick = {
                            when (key.code) {
                                KEYCODE_CLEAR -> viewModel.onAction(KeyboardAction.ClearComposition)
                                else -> viewModel.onAction(KeyboardAction.KeyPress(key.code))
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                    )
                }
            }
        }

        // 底部工具栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            T9Layout.bottomToolbar.forEach { key ->
                val icon = when (key.code) {
                    KEYCODE_SWITCH_LANG -> Icons.Outlined.Language
                    else -> null
                }
                val subLabel = when (key.code) {
                    KEYCODE_SWITCH_LANG -> if (state.activeKeyboard == KeyboardType.ENGLISH) "EN" else "中"
                    else -> null
                }
                GlassKeyButton(
                    label = if (icon != null) "" else key.label,
                    icon = icon,
                    subLabel = subLabel,
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
            }
        }

        // 底部留白（适配曲面屏手机）
        Spacer(modifier = Modifier.height(28.dp))
    }
}
