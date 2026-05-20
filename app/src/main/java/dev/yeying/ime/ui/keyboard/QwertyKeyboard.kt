package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.KeyboardCapslock
import androidx.compose.material.icons.outlined.KeyboardReturn
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val NUMBER_ROW = listOf("1","2","3","4","5","6","7","8","9","0")

@Composable
fun QwertyKeyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

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
                    val label: String
                    val icon = when (key.code) {
                        KEYCODE_SHIFT -> { label = ""; Icons.Outlined.KeyboardCapslock }
                        KEYCODE_DELETE -> { label = ""; Icons.Outlined.Backspace }
                        KEYCODE_ENTER -> { label = ""; Icons.Outlined.KeyboardReturn }
                        KEYCODE_SWITCH_LANG -> {
                            label = ""
                            Icons.Outlined.Language
                        }
                        KEYCODE_SPACE -> { label = ""; Icons.Outlined.SpaceBar }
                        else -> { label = key.label; null }
                    }
                    val subLabel = when (key.code) {
                        KEYCODE_SWITCH_LANG -> if (state.activeKeyboard == KeyboardType.ENGLISH) "EN" else "中"
                        else -> null
                    }
                    GlassKeyButton(
                        label = label,
                        onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                        icon = icon,
                        subLabel = subLabel,
                        width = key.width,
                        modifier = if (key.width == 0.dp) Modifier.weight(1f) else Modifier,
                        height = 42.dp,
                        isActive = key.code == KEYCODE_SHIFT && state.capsState != CapsState.NONE,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
