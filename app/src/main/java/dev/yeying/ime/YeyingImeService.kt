package dev.yeying.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.yeying.ime.bridge.ComposeBridge
import dev.yeying.ime.engine.RimeEngine
import dev.yeying.ime.ui.keyboard.KeyboardViewModel
import dev.yeying.ime.ui.theme.YeyingTheme

class YeyingImeService : InputMethodService() {

    private val bridge = ComposeBridge()
    private var keyboardViewModel: KeyboardViewModel? = null

    override fun onCreate() {
        super.onCreate()
        bridge.onCreate()
        
        // Set owners on the window decor view to ensure Compose popups/dialogs work
        window?.window?.let { win ->
            val decorView = win.decorView
            decorView.setViewTreeLifecycleOwner(bridge)
            decorView.setViewTreeViewModelStoreOwner(bridge)
            decorView.setViewTreeSavedStateRegistryOwner(bridge)
            
            // Apply background blur for Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                win.setBackgroundBlurRadius(60)
            }
            win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }

        RimeEngine.instance.startup(this)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        bridge.onStart()
        return bridge.createComposeView(composeView) {
            YeyingTheme {
                YeyingKeyboard(
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
    }

    override fun onStartInput(attribute: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        keyboardViewModel?.resetToHome()
    }

    override fun onDestroy() {
        bridge.onDestroy()
        RimeEngine.instance.shutdown()
        super.onDestroy()
    }
}
