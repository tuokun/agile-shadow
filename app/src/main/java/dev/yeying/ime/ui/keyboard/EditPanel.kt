package dev.yeying.ime.ui.keyboard

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.LastPage
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FirstPage
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val KeyH = 52.dp

@Composable
fun EditPanel(
    onSendKey: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 左区域：十字方向键 + 句首句尾
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 十字方向键：左右高度 = 中心列全高，中线自然对齐「选择」
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    GlassKeyButton(
                        label = "",
                        icon = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        onClick = { onSendKey(KeyEvent.KEYCODE_DPAD_LEFT) },
                        modifier = Modifier.weight(1f),
                        height = KeyH * 3 + 8.dp,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        GlassKeyButton(
                            label = "",
                            icon = Icons.Outlined.KeyboardArrowUp,
                            onClick = { onSendKey(KeyEvent.KEYCODE_DPAD_UP) },
                            modifier = Modifier.fillMaxWidth(),
                            height = KeyH,
                        )
                        GlassKeyButton(
                            label = "选择",
                            onClick = { onSendKey(android.R.id.edit) },
                            modifier = Modifier.fillMaxWidth(),
                            height = KeyH,
                        )
                        GlassKeyButton(
                            label = "",
                            icon = Icons.Outlined.KeyboardArrowDown,
                            onClick = { onSendKey(KeyEvent.KEYCODE_DPAD_DOWN) },
                            modifier = Modifier.fillMaxWidth(),
                            height = KeyH,
                        )
                    }
                    GlassKeyButton(
                        label = "",
                        icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        onClick = { onSendKey(KeyEvent.KEYCODE_DPAD_RIGHT) },
                        modifier = Modifier.weight(1f),
                        height = KeyH * 3 + 8.dp,
                    )
                }
                // 句首 + 句尾（与右侧删除对齐）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    GlassKeyButton(
                        label = "句首",
                        icon = Icons.Outlined.FirstPage,
                        subLabel = "句首",
                        onClick = { onSendKey(KeyEvent.KEYCODE_MOVE_HOME) },
                        modifier = Modifier.weight(1f),
                        height = 44.dp,
                    )
                    GlassKeyButton(
                        label = "句尾",
                        icon = Icons.AutoMirrored.Outlined.LastPage,
                        subLabel = "句尾",
                        onClick = { onSendKey(KeyEvent.KEYCODE_MOVE_END) },
                        modifier = Modifier.weight(1f),
                        height = 44.dp,
                    )
                }
            }

            // 右区域：复制 + 粘贴 + 剪切 + 删除
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                GlassKeyButton(
                    label = "复制",
                    icon = Icons.Outlined.ContentCopy,
                    subLabel = "复制",
                    onClick = { onSendKey(android.R.id.copy) },
                    modifier = Modifier.fillMaxWidth(),
                    height = KeyH,
                )
                GlassKeyButton(
                    label = "粘贴",
                    icon = Icons.Outlined.ContentPaste,
                    subLabel = "粘贴",
                    onClick = { onSendKey(android.R.id.paste) },
                    modifier = Modifier.fillMaxWidth(),
                    height = KeyH,
                )
                GlassKeyButton(
                    label = "剪切",
                    icon = Icons.Outlined.ContentCut,
                    subLabel = "剪切",
                    onClick = { onSendKey(android.R.id.cut) },
                    modifier = Modifier.fillMaxWidth(),
                    height = KeyH,
                )
                GlassKeyButton(
                    label = "删除",
                    icon = Icons.AutoMirrored.Outlined.Backspace,
                    subLabel = "删除",
                    onClick = { onSendKey(KeyEvent.KEYCODE_DEL) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 44.dp,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
