package dev.yeying.ime.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.yeying.ime.ui.keyboard.KeyboardAction
import dev.yeying.ime.ui.keyboard.KeyboardViewModel

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
) {
    CategorizedGridPanel(
        categories = symbolCategories,
        onItemClick = { viewModel.onAction(KeyboardAction.DirectCommit(it)) },
        modifier = modifier,
    )
}
