package dev.yeying.ime.ui.keyboard

import com.yuyan.inputmethod.core.CandidateListItem

enum class KeyboardType { T9, QWERTY, ENGLISH, SYMBOL, HANDWRITING }
enum class CapsState { NONE, ONCE, LOCK }

data class KeyboardState(
    val activeKeyboard: KeyboardType = KeyboardType.QWERTY,
    val candidates: List<CandidateListItem> = emptyList(),
    val composingText: String = "",
    val capsState: CapsState = CapsState.NONE,
    val symbolPage: Int = 0,
    val hasNextPage: Boolean = false,
    val page: Int = 0,
)

sealed class KeyboardAction {
    data class KeyPress(val keycode: Int, val mask: Int = 0) : KeyboardAction()
    data class CandidateSelect(val index: Int) : KeyboardAction()
    data class SwitchKeyboard(val type: KeyboardType) : KeyboardAction()
    data object ClearComposition : KeyboardAction()
}
