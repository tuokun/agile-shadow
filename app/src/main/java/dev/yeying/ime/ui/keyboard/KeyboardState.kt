package dev.yeying.ime.ui.keyboard

import com.yuyan.inputmethod.core.CandidateListItem

enum class KeyboardType { T9, QWERTY, ENGLISH, NUMBER, SYMBOL, EMOJI, HANDWRITING, TOOLS, KEYBOARD_PICKER, EDIT, CLIPBOARD }
enum class CapsState { NONE, ONCE, LOCK }

data class KeyboardState(
    val activeKeyboard: KeyboardType = KeyboardType.T9,
    val candidates: List<CandidateListItem> = emptyList(),
    val composingText: String = "",
    val capsState: CapsState = CapsState.NONE,
    val hasNextPage: Boolean = false,
    val page: Int = 0,
    val candidatesExpanded: Boolean = false,
    val pinyins: List<String> = emptyList(),
    val previousKeyboard: KeyboardType = KeyboardType.T9,
    val clipboardSuggestion: String? = null,
)

sealed class KeyboardAction {
    data class KeyPress(val keycode: Int, val mask: Int = 0) : KeyboardAction()
    data class CandidateSelect(val index: Int) : KeyboardAction()
    data class SwitchKeyboard(val type: KeyboardType) : KeyboardAction()
    data object ClearComposition : KeyboardAction()
    data class DirectCommit(val text: String) : KeyboardAction()
    data class SelectPinyin(val index: Int) : KeyboardAction()
}
