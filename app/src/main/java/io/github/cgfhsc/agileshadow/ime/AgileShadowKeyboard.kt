package io.github.cgfhsc.agileshadow.ime

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.cgfhsc.agileshadow.ime.data.Prefs
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DARK_KEYBOARD_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DEFAULT_TEXT
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KEYBOARD_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.EditPanel
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import io.github.cgfhsc.agileshadow.ime.ui.candidate.ComposingTag
import io.github.cgfhsc.agileshadow.ime.ui.candidate.ExpandedCandidateView
import io.github.cgfhsc.agileshadow.ime.ui.candidate.ToolbarCandidateBar
import io.github.cgfhsc.agileshadow.ime.data.clipboard.ClipboardPanel
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.HandwritingBoard
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardPickerPanel
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardType
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardViewModel
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.NumberKeyboard
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.QwertyKeyboard
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.T9Keyboard
import io.github.cgfhsc.agileshadow.ime.ui.symbol.EmojiPanel
import io.github.cgfhsc.agileshadow.ime.ui.symbol.SymbolPanel
import io.github.cgfhsc.agileshadow.ime.ui.theme.AgileShadowTheme
import io.github.cgfhsc.agileshadow.ime.ui.tools.ToolsPanel

private val FULL_PANEL_TYPES = setOf(KeyboardType.SYMBOL, KeyboardType.EMOJI)

@Composable
fun AgileShadowKeyboard(
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

    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val darkTheme by prefs.darkTheme.collectAsState(initial = false)
    val followSystem by prefs.followSystemTheme.collectAsState(initial = false)
    val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    val isDark = if (followSystem) isSystemDark else darkTheme

    val isFullPanel = state.candidatesExpanded || state.activeKeyboard in FULL_PANEL_TYPES

    val showComposing = state.composingDisplay.isNotEmpty()

    AgileShadowTheme(isDark = isDark) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()
            .drawBehind {
                val color = if (isDark) Color(0xFF2E2E32) else Color(0xFFD5D5D5)
                drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
            }
            .background(if (isDark) DARK_KEYBOARD_BG else KEYBOARD_BG)
        ) {
            if (!isFullPanel) {
                ToolbarCandidateBar(viewModel, isDark = isDark, onHideKeyboard = onHideKeyboard, onCommitText = onCommitText)
            }

            Box(modifier = Modifier.fillMaxWidth().height(if (isFullPanel) 310.dp else 260.dp)) {
                if (state.candidatesExpanded) {
                    ExpandedCandidateView(
                        viewModel = viewModel,
                        isDark = isDark,
                        isHandwriting = state.handwritingCandidates.isNotEmpty(),
                    )
                } else when (state.activeKeyboard) {
                    KeyboardType.QWERTY, KeyboardType.ENGLISH -> QwertyKeyboard(viewModel, isDark = isDark)
                    KeyboardType.T9 -> T9Keyboard(viewModel, isDark = isDark)
                    KeyboardType.NUMBER -> NumberKeyboard(viewModel, isDark = isDark)
                    KeyboardType.SYMBOL -> SymbolPanel(viewModel, isDark = isDark)
                    KeyboardType.EMOJI -> EmojiPanel(viewModel, isDark = isDark)
                    KeyboardType.HANDWRITING -> HandwritingBoard(
                        viewModel = viewModel,
                        isDark = isDark,
                        onCommitText = onCommitText,
                        onDeleteChar = onDeleteChar,
                    )
                    KeyboardType.TOOLS -> ToolsPanel(viewModel, isDark = isDark)
                    KeyboardType.KEYBOARD_PICKER -> KeyboardPickerPanel(viewModel, isDark = isDark)
                    KeyboardType.EDIT -> EditPanel(isDark = isDark, onSendKey = onPerformAction)
                    KeyboardType.CLIPBOARD -> ClipboardPanel(onCommitText = onCommitText)
                }
            }
        }

        if (showComposing) {
            var tagHeight by remember { mutableStateOf(0) }
            Popup(alignment = Alignment.TopStart, offset = IntOffset(0, -tagHeight)) {
                Box(modifier = Modifier.onSizeChanged { tagHeight = it.height }) {
                    ComposingTag(
                        state.composingDisplay,
                        if (isDark) Color.White else DEFAULT_TEXT,
                        isDark,
                        Modifier,
                    )
                }
            }
        }
    }
    }
}
