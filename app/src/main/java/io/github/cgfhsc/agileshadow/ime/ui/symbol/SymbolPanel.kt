package io.github.cgfhsc.agileshadow.ime.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardAction
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardViewModel

private val symbolCategories = listOf(
    SymbolGridCategory("常用", listOf(
        "，", "。", "！", "？", "、", "：", "；", """, """, """, """,
        "（", "）", "【", "】", "《", "》", "——", "……", "·", "～",
        "￥", "$", "€", "£", "%", "&", "@", "#", "^", "*",
    )),
    SymbolGridCategory("中文", listOf(
        "，", "。", "、", "：", "；", "？", "！", """, """, """, """,
        "（", "）", "【", "】", "《", "》", "〈", "〉", "〔", "〕",
        "——", "……", "·", "～", "「", "」", "『", "』", "【", "】",
    )),
    SymbolGridCategory("英文", listOf(
        ",", ".", "!", "?", ";", ":", "'", "\"", "(", ")",
        "[", "]", "{", "}", "<", ">", "/", "\\", "@", "#",
        "$", "%", "^", "&", "*", "+", "-", "=", "_", "|",
    )),
    SymbolGridCategory("特殊", listOf(
        "℃", "℉", "°", "±", "×", "÷", "≠", "≈", "≤", "≥",
        "∞", "√", "∑", "∏", "∫", "Δ", "π", "Ω", "μ", "†",
        "§", "№", "※", "☆", "★", "○", "●", "◇", "◆", "□",
    )),
)

@Composable
fun SymbolPanel(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    var isLocked by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    CategorizedGridPanel(
        categories = symbolCategories,
        onItemClick = { symbol ->
            viewModel.onAction(KeyboardAction.DirectCommit(symbol))
            if (!isLocked) {
                viewModel.onAction(KeyboardAction.SwitchKeyboard(state.previousKeyboard))
            }
        },
        modifier = modifier,
        isDark = isDark,
        columns = 4,
        onBack = { viewModel.onAction(KeyboardAction.SwitchKeyboard(state.previousKeyboard)) },
        isLocked = isLocked,
        onLockToggle = { isLocked = !isLocked },
    )
}
