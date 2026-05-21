package io.github.cgfhsc.agileshadow.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardPickerPanel(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val options = listOf(
            KeyboardType.T9 to "拼音9键",
            KeyboardType.QWERTY to "拼音26键",
            KeyboardType.HANDWRITING to "手写输入",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { (type, label) ->
                GlassKeyButton(
                    isDark = isDark,
                    label = label,
                    onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(type)) },
                    modifier = Modifier.weight(1f),
                    height = 52.dp,
                    isActive = state.lastChineseKeyboard == type,
                    keyBackgroundColor = TOOLBAR_BG,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
