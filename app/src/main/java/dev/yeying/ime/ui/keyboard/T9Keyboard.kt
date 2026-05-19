package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        T9Layout.rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { key ->
                    T9KeyButton(
                        key = key,
                        onClick = {
                            when (key.code) {
                                KEYCODE_CLEAR -> viewModel.onAction(KeyboardAction.ClearComposition)
                                KEYCODE_DELETE -> viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_DELETE))
                                KEYCODE_SHIFT -> { /* TODO: shift toggle */ }
                                else -> viewModel.onAction(KeyboardAction.KeyPress(key.code))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun T9KeyButton(
    key: T9Layout.T9Key,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = key.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (key.subLabel.isNotEmpty()) {
                Text(
                    text = key.subLabel,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
