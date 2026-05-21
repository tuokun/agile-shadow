package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val CENTER_KEY_HEIGHT = 52.dp
private val CENTER_ROW_GAP = 4.dp
private val MAIN_HEIGHT = CENTER_KEY_HEIGHT * 3 + CENTER_ROW_GAP * 2 // 164dp

@Composable
fun NumberKeyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 主区域：三列（左符号 + 中数字 + 右功能）
        Row(
            modifier = Modifier.fillMaxWidth().height(MAIN_HEIGHT),
            horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
        ) {
            // 左列：* / - + 四个独立键
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                listOf("*", "/", "-", "+").forEach { sym ->
                    GlassKeyButton(
                        label = sym,
                        onClick = { viewModel.onAction(KeyboardAction.DirectCommit(sym)) },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                }
            }

            // 中列：数字 3x3
            Column(
                modifier = Modifier.weight(4f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                ).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(CENTER_KEY_HEIGHT),
                        horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
                    ) {
                        row.forEach { ch ->
                            GlassKeyButton(
                                label = ch,
                                onClick = { viewModel.onAction(KeyboardAction.DirectCommit(ch)) },
                                modifier = Modifier.weight(1f),
                                height = CENTER_KEY_HEIGHT,
                            )
                        }
                    }
                }
            }

            // 右列：删除、.、@
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                GlassKeyButton(
                    label = "",
                    icon = Icons.Outlined.Backspace,
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_DELETE)) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    height = 0.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
                GlassKeyButton(
                    label = ".",
                    onClick = { viewModel.onAction(KeyboardAction.DirectCommit(".")) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    height = 0.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
                GlassKeyButton(
                    label = "@",
                    onClick = { viewModel.onAction(KeyboardAction.DirectCommit("@")) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    height = 0.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
            }
        }

        // 底部工具栏（镜像上方三列结构：左1 + 中3 + 右1）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassKeyButton(
                label = "符号",
                onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.SYMBOL)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
                keyBackgroundColor = TOOLBAR_BG,
            )
            Row(
                modifier = Modifier.weight(4f),
                horizontalArrangement = Arrangement.spacedBy(CENTER_ROW_GAP),
            ) {
                GlassKeyButton(
                    label = "返回",
                    onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(viewModel.state.value.previousKeyboard)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
                GlassKeyButton(
                    label = "",
                    icon = Icons.Outlined.SpaceBar,
                    onClick = { viewModel.onAction(KeyboardAction.DirectCommit(" ")) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
                GlassKeyButton(
                    label = "0",
                    onClick = { viewModel.onAction(KeyboardAction.DirectCommit("0")) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    keyBackgroundColor = TOOLBAR_BG,
                )
            }
            GlassKeyButton(
                label = "确认",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_ENTER)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
                keyBackgroundColor = CONFIRM_BG,
                textColor = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
