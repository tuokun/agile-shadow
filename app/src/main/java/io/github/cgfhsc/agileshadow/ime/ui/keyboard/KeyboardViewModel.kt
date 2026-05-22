package io.github.cgfhsc.agileshadow.ime.ui.keyboard

import androidx.lifecycle.ViewModel
import com.yuyan.inputmethod.core.CandidateListItem
import com.yuyan.inputmethod.core.Rime
import io.github.cgfhsc.agileshadow.ime.engine.InputKeyTracker
import io.github.cgfhsc.agileshadow.ime.engine.InputRecord
import io.github.cgfhsc.agileshadow.ime.engine.RimeEngine
import io.github.cgfhsc.agileshadow.ime.engine.T9Mapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class KeyboardViewModel(
    private val onCommitText: (String) -> Unit = {},
    private val onDeleteChar: () -> Unit = {},
    private val onSendEnter: () -> Unit = {},
) : ViewModel() {

    companion object {
        private const val SCHEMA_T9 = "t9"
        private const val SCHEMA_RIME_ICE = "rime_ice"
    }

    private val backspaceKeycode by lazy { Rime.getRimeKeycodeByName("BackSpace") }
    private val pageDownKeycode by lazy { Rime.getRimeKeycodeByName("Page_Down") }

    private val _state = MutableStateFlow(KeyboardState())
    val state: StateFlow<KeyboardState> = _state

    // 累积候选词（跨页追加，新输入时重置）
    private var accumulatedCandidates = mutableListOf<CandidateListItem>()
    private var lastComposingText = ""
    private val inputKeyTracker = InputKeyTracker()
    private var lastChineseKeyboard = KeyboardType.T9

    fun onAction(action: KeyboardAction) {
        when (action) {
            is KeyboardAction.KeyPress -> handleKeyPress(action)
            is KeyboardAction.CandidateSelect -> handleCandidateSelect(action)
            is KeyboardAction.SwitchKeyboard -> handleSwitchKeyboard(action)
            is KeyboardAction.ClearComposition -> handleClearComposition()
            is KeyboardAction.DirectCommit -> onCommitText(action.text)
            is KeyboardAction.SelectPinyin -> handleSelectPinyin(action)
        }
    }

    private fun handleKeyPress(action: KeyboardAction.KeyPress) {
        val engine = RimeEngine.instance
        if (!engine.isInitialized) return

        // 用户开始输入时清除剪贴板建议
        if (_state.value.clipboardSuggestion != null) {
            _state.update { it.copy(clipboardSuggestion = null) }
        }

        when (action.keycode) {
            KEYCODE_DELETE -> {
                if (_state.value.composingText.isNotEmpty()) {
                    val lastRecord = inputKeyTracker.pop()
                    when (lastRecord) {
                        is InputRecord.PinyinKey -> {
                            val restored = inputKeyTracker.restorePinyinToT9Key(lastRecord)
                            if (restored != null) {
                                val t9Keys = restored.t9Keys()
                                if (!engine.replaceKey(restored.posInInput, restored.inputKeyLength, t9Keys)) {
                                    engine.replaceKey(restored.posInInput, restored.pinyinLength, t9Keys)
                                }
                            }
                        }
                        else -> engine.processKey(backspaceKeycode, 0)
                    }
                    engine.commitIfNeeded()?.let { onCommitText(it) }
                    refreshState()
                } else {
                    onDeleteChar()
                }
            }
            KEYCODE_SHIFT -> {
                _state.update { s ->
                    s.copy(capsState = if (s.capsState == CapsState.NONE) CapsState.LOCK else CapsState.NONE)
                }
            }
            KEYCODE_SYMBOL -> {
                _state.update { it.copy(activeKeyboard = KeyboardType.SYMBOL) }
            }
            KEYCODE_SWITCH_LANG -> {
                val current = _state.value.activeKeyboard
                if (current == KeyboardType.ENGLISH) {
                    val target = lastChineseKeyboard
                    if (target == KeyboardType.T9) engine.selectSchema(SCHEMA_T9)
                    _state.update { it.copy(activeKeyboard = target) }
                } else {
                    lastChineseKeyboard = current
                    _state.update { it.copy(activeKeyboard = KeyboardType.ENGLISH, lastChineseKeyboard = current) }
                }
            }
            KEYCODE_NUMBER -> {
                _state.update { it.copy(activeKeyboard = KeyboardType.NUMBER) }
            }
            KEYCODE_ENTER -> {
                if (_state.value.composingText.isNotEmpty()) {
                    engine.clearComposition()
                    inputKeyTracker.clear()
                    accumulatedCandidates.clear()
                }
                onSendEnter()
                refreshState()
            }
            KEYCODE_SPACE -> {
                if (_state.value.composingText.isNotEmpty()) {
                    RimeEngine.instance.selectCandidate(0)
                    val committed = RimeEngine.instance.commitIfNeeded()
                    if (committed != null) {
                        onCommitText(committed)
                        inputKeyTracker.clear()
                    }
                    accumulatedCandidates.clear()
                    refreshState()
                } else {
                    onCommitText(" ")
                }
            }
            else -> {
                val caps = _state.value.capsState
                if (caps != CapsState.NONE) {
                    onCommitText(action.keycode.toChar().uppercaseChar().toString())
                } else if (_state.value.activeKeyboard == KeyboardType.ENGLISH) {
                    onCommitText(action.keycode.toChar().toString())
                } else {
                    engine.processKey(action.keycode, action.mask)
                    if (_state.value.activeKeyboard == KeyboardType.T9) {
                        inputKeyTracker.pushT9Key(action.keycode)
                    }
                    engine.commitIfNeeded()?.let { onCommitText(it) }
                    refreshState()
                }
            }
        }
    }

    private fun handleCandidateSelect(action: KeyboardAction.CandidateSelect) {
        val hwCandidates = _state.value.handwritingCandidates
        if (hwCandidates.isNotEmpty()) {
            val text = hwCandidates.getOrElse(action.index) { null }
            if (text != null) {
                onCommitText(text)
                clearHandwritingCandidates()
                _state.update { it.copy(candidatesExpanded = false) }
            }
            return
        }

        RimeEngine.instance.selectCandidate(action.index)
        val committed = RimeEngine.instance.commitIfNeeded()
        if (committed != null) {
            onCommitText(committed)
            inputKeyTracker.clear()
        }
        accumulatedCandidates.clear()
        refreshState()

        if (_state.value.composingText.isEmpty() && _state.value.candidates.isEmpty()) {
            _state.update { it.copy(candidatesExpanded = false) }
        }
    }

    private fun handleSwitchKeyboard(action: KeyboardAction.SwitchKeyboard) {
        val engine = RimeEngine.instance

        if (engine.isInitialized) {
            when {
                action.type == KeyboardType.T9 -> engine.selectSchema(SCHEMA_T9)
                action.type.isPanel -> { /* keep current schema */ }
                _state.value.activeKeyboard == KeyboardType.T9 -> {
                    engine.selectSchema(SCHEMA_RIME_ICE)
                }
            }
        }

        _state.update { s ->
            val prev = if (action.type.isPanel && !s.activeKeyboard.isPanel) s.activeKeyboard else s.previousKeyboard
            s.copy(activeKeyboard = action.type, previousKeyboard = prev, candidatesExpanded = false)
        }

        when (action.type) {
            KeyboardType.T9, KeyboardType.QWERTY, KeyboardType.HANDWRITING -> {
                lastChineseKeyboard = action.type
                _state.update { it.copy(lastChineseKeyboard = action.type) }
            }
            else -> {}
        }
    }

    private fun handleSelectPinyin(action: KeyboardAction.SelectPinyin) {
        val engine = RimeEngine.instance
        if (!engine.isInitialized) return
        if (action.index < 0 || action.index >= _state.value.pinyins.size) return

        val pinyin = _state.value.pinyins[action.index]
        val pinyinKey = inputKeyTracker.pushPinyinSelectAction(pinyin) ?: return

        engine.replaceKey(pinyinKey.posInInput, pinyinKey.t9Keys().length, pinyinKey.rimeKey())
        engine.commitIfNeeded()?.let { onCommitText(it) }
        refreshState()
    }

    private fun handleClearComposition() {
        RimeEngine.instance.clearComposition()
        accumulatedCandidates.clear()
        inputKeyTracker.clear()
        refreshState()
    }

    private fun refreshState(isPaging: Boolean = false) {
        val ctx = RimeEngine.instance.getContext()
        val newComposingText = ctx?.composition?.preedit ?: ""
        val newPageCandidates = ctx?.candidates?.toList() ?: emptyList()
        val hasNext = ctx?.menu?.isLastPage == false
        val newPage = ctx?.menu?.pageNo ?: 0

        if (!isPaging && newComposingText != lastComposingText) {
            accumulatedCandidates = newPageCandidates.toMutableList()
            lastComposingText = newComposingText
        } else if (newPage != _state.value.page && newPageCandidates.isNotEmpty()) {
            accumulatedCandidates.addAll(newPageCandidates)
        }

        inputKeyTracker.updateConsumedFlags(newComposingText)
        val unresolvedT9 = inputKeyTracker.getUnresolvedT9Sequence()
        val pinyinList = if (unresolvedT9.isNotEmpty() && _state.value.activeKeyboard == KeyboardType.T9) {
            T9Mapper.t9ToPinyin(unresolvedT9).toList()
        } else {
            emptyList()
        }

        _state.update { s ->
            s.copy(
                candidates = accumulatedCandidates.toList(),
                composingText = newComposingText,
                hasNextPage = hasNext,
                page = newPage,
                pinyins = pinyinList,
            )
        }
    }

    fun resetToHome() {
        val engine = RimeEngine.instance
        val target = lastChineseKeyboard
        if (engine.isInitialized) {
            engine.clearComposition()
            when (target) {
                KeyboardType.T9 -> engine.selectSchema(SCHEMA_T9)
                else -> engine.selectSchema(SCHEMA_RIME_ICE)
            }
        }
        accumulatedCandidates.clear()
        lastComposingText = ""
        inputKeyTracker.clear()
        _state.update { it.copy(
            activeKeyboard = target,
            lastChineseKeyboard = target,
            candidates = emptyList(),
            composingText = "",
            hasNextPage = false,
            page = 0,
            pinyins = emptyList(),
            clipboardSuggestion = null,
            handwritingCandidates = emptyList(),
            capsState = CapsState.NONE,
        ) }
    }

    fun nextPage() {
        val engine = RimeEngine.instance
        if (engine.isInitialized && _state.value.hasNextPage) {
            engine.processKey(pageDownKeycode, 0)
            refreshState(isPaging = true)
        }
    }

    fun toggleCandidatesExpanded() {
        _state.update { it.copy(candidatesExpanded = !it.candidatesExpanded) }
    }

    fun setClipboardSuggestion(text: String?) {
        _state.update { it.copy(clipboardSuggestion = text) }
    }

    fun setHandwritingCandidates(candidates: List<String>) {
        accumulatedCandidates.clear()
        lastComposingText = ""
        _state.update { s ->
            s.copy(
                candidates = candidates.map { CandidateListItem(it, it) },
                composingText = "",
                handwritingCandidates = candidates,
                hasNextPage = false,
                page = 0,
            )
        }
    }

    fun clearHandwritingCandidates() {
        _state.update { s ->
            s.copy(candidates = emptyList(), composingText = "", handwritingCandidates = emptyList())
        }
    }
}
