package dev.yeying.ime

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yeying.ime.ui.keyboard.EditPanel
import dev.yeying.ime.ui.candidate.ExpandedCandidateView
import dev.yeying.ime.ui.candidate.ToolbarCandidateBar
import dev.yeying.ime.data.clipboard.ClipboardPanel
import dev.yeying.ime.ui.keyboard.HandwritingBoard
import dev.yeying.ime.ui.keyboard.KeyboardPickerPanel
import dev.yeying.ime.ui.keyboard.KeyboardType
import dev.yeying.ime.ui.keyboard.KeyboardViewModel
import dev.yeying.ime.ui.keyboard.NumberKeyboard
import dev.yeying.ime.ui.keyboard.QwertyKeyboard
import dev.yeying.ime.ui.keyboard.T9Keyboard
import dev.yeying.ime.ui.symbol.EmojiPanel
import dev.yeying.ime.ui.symbol.SymbolPanel
import dev.yeying.ime.ui.tools.ToolsPanel
import dev.yeying.ime.ui.theme.liquidGlass

@Composable
fun YeyingKeyboard(
    onCommitText: (String) -> Unit = {},
    onDeleteChar: () -> Unit = {},
    onSendEnter: () -> Unit = {},
    onPerformAction: (Int) -> Unit = {},
    onHideKeyboard: () -> Unit = {},
    onViewModelReady: (KeyboardViewModel) -> Unit = {},
) {
    val viewModel = remember {
        KeyboardViewModel(
            onCommitText = onCommitText,
            onDeleteChar = onDeleteChar,
            onSendEnter = onSendEnter,
        ).also { onViewModelReady(it) }
    }

    val state by viewModel.state.collectAsState()
    val isDark = isSystemInDarkTheme()

    Column(modifier = Modifier.fillMaxWidth().liquidGlass(cornerRadius = 0.dp, isDark = isDark)) {
        ToolbarCandidateBar(viewModel, onHideKeyboard = onHideKeyboard, onCommitText = onCommitText)

        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            if (state.candidatesExpanded) {
                ExpandedCandidateView(viewModel)
            } else when (state.activeKeyboard) {
                KeyboardType.QWERTY, KeyboardType.ENGLISH -> QwertyKeyboard(viewModel)
                KeyboardType.T9 -> T9Keyboard(viewModel)
                KeyboardType.NUMBER -> NumberKeyboard(viewModel)
                KeyboardType.SYMBOL -> SymbolPanel(viewModel)
                KeyboardType.EMOJI -> EmojiPanel(viewModel)
                KeyboardType.HANDWRITING -> HandwritingBoard(
                    onCandidateSelected = onCommitText,
                )
                KeyboardType.TOOLS -> ToolsPanel(viewModel)
                KeyboardType.KEYBOARD_PICKER -> KeyboardPickerPanel(viewModel)
                KeyboardType.EDIT -> EditPanel(onSendKey = onPerformAction)
                KeyboardType.CLIPBOARD -> ClipboardPanel(onCommitText = onCommitText)
            }
        }
    }
}
