package dev.yeying.ime.ui.keyboard

import androidx.lifecycle.ViewModel
import dev.yeying.ime.engine.RimeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class KeyboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(KeyboardState())
    val state: StateFlow<KeyboardState> = _state

    fun onAction(action: KeyboardAction) {
        when (action) {
            is KeyboardAction.KeyPress -> handleKeyPress(action)
            is KeyboardAction.CandidateSelect -> handleCandidateSelect(action)
            is KeyboardAction.SwitchKeyboard -> handleSwitchKeyboard(action)
            is KeyboardAction.ClearComposition -> handleClearComposition()
        }
    }

    private fun handleKeyPress(action: KeyboardAction.KeyPress) {
        val engine = RimeEngine.instance
        if (!engine.isInitialized) return

        engine.processKey(action.keycode, action.mask)

        val commit = engine.getCommit()
        if (commit != null && commit.commitText.isNotEmpty()) {
            engine.clearComposition()
        }

        refreshState()
    }

    private fun handleCandidateSelect(action: KeyboardAction.CandidateSelect) {
        RimeEngine.instance.selectCandidate(action.index)
        val commit = RimeEngine.instance.getCommit()
        if (commit != null && commit.commitText.isNotEmpty()) {
            // TODO: 提交文本
        }
        refreshState()
    }

    private fun handleSwitchKeyboard(action: KeyboardAction.SwitchKeyboard) {
        _state.update { it.copy(activeKeyboard = action.type) }
    }

    private fun handleClearComposition() {
        RimeEngine.instance.clearComposition()
        refreshState()
    }

    private fun refreshState() {
        val ctx = RimeEngine.instance.getContext()
        _state.update { s ->
            s.copy(
                candidates = ctx?.candidates?.toList() ?: emptyList(),
                composingText = ctx?.composition?.preedit ?: "",
                hasNextPage = ctx?.menu?.isLastPage == false,
                page = ctx?.menu?.pageNo ?: 0,
            )
        }
    }
}
