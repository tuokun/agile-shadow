package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberKeyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("-", "0", "."),
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { ch ->
                    GlassKeyButton(
                        label = ch,
                        onClick = { viewModel.onAction(KeyboardAction.DirectCommit(ch)) },
                        modifier = Modifier.weight(1f),
                        height = 52.dp,
                    )
                }
            }
        }

        // 底部：删除 + 确认
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassKeyButton(
                label = "⌫",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_DELETE)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
            )
            GlassKeyButton(
                label = "确认",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_ENTER)) },
                modifier = Modifier.weight(2f),
                height = 44.dp,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
