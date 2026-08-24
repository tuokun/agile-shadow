package io.github.cgfhsc.agileshadow.ime

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.cgfhsc.agileshadow.ime.bridge.ComposeBridge
import io.github.cgfhsc.agileshadow.ime.data.clipboard.ClipboardDatabase
import io.github.cgfhsc.agileshadow.ime.data.clipboard.ClipboardItem
import io.github.cgfhsc.agileshadow.ime.engine.HandwritingEngine
import io.github.cgfhsc.agileshadow.ime.engine.RimeEngine
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AgileShadowImeService : InputMethodService() {

    private val bridge = ComposeBridge()
    private var keyboardViewModel: KeyboardViewModel? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var clipboardDb: ClipboardDatabase
    private lateinit var clipboardManager: ClipboardManager
    private var navigationBarBottom by mutableIntStateOf(0)

    // 缓存最近复制内容，用于候选栏建议
    private var lastClipboardText: String? = null
    private var lastClipboardTime: Long = 0

    private val clipboardListener = object : ClipboardManager.OnPrimaryClipChangedListener {
        override fun onPrimaryClipChanged() {
            val clip = clipboardManager.primaryClip ?: return
            if (clip.itemCount == 0) return
            val text = clip.getItemAt(0)?.text?.toString() ?: return
            if (text.isBlank()) return

            Log.d("AgileShadowImeService", "Clipboard changed: ${text.take(20)}...")
            lastClipboardText = text
            lastClipboardTime = System.currentTimeMillis()

            scope.launch {
                try {
                    val dao = clipboardDb.clipboardDao()
                    if (dao.getLatestText() == text) return@launch
                    dao.insert(ClipboardItem(text = text))
                    dao.trimToLimit()
                    Log.d("AgileShadowImeService", "Clipboard saved")
                } catch (e: Exception) {
                    Log.e("AgileShadowImeService", "Failed to save clipboard", e)
                }
            }
            keyboardViewModel?.setClipboardSuggestion(text)
        }
    }

    override fun onCreate() {
        super.onCreate()
        bridge.onCreate()

        clipboardDb = ClipboardDatabase.getInstance(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)

        window?.window?.let { win ->
            val decorView = win.decorView
            decorView.setViewTreeLifecycleOwner(bridge)
            decorView.setViewTreeViewModelStoreOwner(bridge)
            decorView.setViewTreeSavedStateRegistryOwner(bridge)

            win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }

        scope.launch { RimeEngine.instance.startup(this@AgileShadowImeService) }
        scope.launch { HandwritingEngine.getInstance().init(this@AgileShadowImeService) }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        bridge.onStart()
        return bridge.createComposeView(composeView) {
            AgileShadowKeyboard(
                    onCommitText = { text ->
                        currentInputConnection?.commitText(text, 1)
                    },
                    onDeleteChar = {
                        currentInputConnection?.let { ic ->
                            val selected = ic.getSelectedText(0)
                            if (!selected.isNullOrEmpty()) {
                                ic.commitText("", 1)
                            } else {
                                ic.deleteSurroundingText(1, 0)
                            }
                        }
                    },
                    onSendEnter = {
                        sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_ENTER)
                    },
                    onHideKeyboard = { requestHideSelf(0) },
                    navigationBarBottom = navigationBarBottom,
                    onNavigationBarThemeChanged = { isDark ->
                        window?.window?.let { win ->
                            win.navigationBarColor = android.graphics.Color.parseColor(
                                if (isDark) "#17181A" else "#EEF0F3",
                            )
                            @Suppress("DEPRECATION")
                            win.decorView.systemUiVisibility = if (isDark) 0 else
                                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                    },
                    onViewModelReady = { keyboardViewModel = it },
                    onPerformAction = { action ->
                        when (action) {
                            android.R.id.selectAll, android.R.id.cut, android.R.id.copy, android.R.id.paste -> {
                                currentInputConnection?.performContextMenuAction(action)
                            }
                            android.R.id.edit -> {
                                currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_LEFT, android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                            android.view.KeyEvent.KEYCODE_DPAD_UP, android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                            android.view.KeyEvent.KEYCODE_MOVE_HOME, android.view.KeyEvent.KEYCODE_MOVE_END,
                            android.view.KeyEvent.KEYCODE_DEL -> {
                                sendDownUpKeyEvents(action)
                            }
                            else -> currentInputConnection?.performEditorAction(action)
                        }
                    },
                )
            }
    }

    override fun onStartInputView(attribute: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        val decorView = window?.window?.decorView
        decorView?.post {
            navigationBarBottom = decorView.rootWindowInsets
                ?.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars())
                ?.bottom
                ?: 0
        }
        if (!restarting) keyboardViewModel?.resetToHome()

        // 键盘弹出时，30秒内复制的文字显示在候选栏
        val text = lastClipboardText
        if (text != null && System.currentTimeMillis() - lastClipboardTime < 30_000L) {
            keyboardViewModel?.setClipboardSuggestion(text)
            lastClipboardText = null
        }
    }

    override fun onDestroy() {
        scope.cancel()
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        HandwritingEngine.getInstance().release()
        bridge.onDestroy()
        RimeEngine.instance.shutdown()
        super.onDestroy()
    }
}
