package dev.yeying.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.ui.platform.ComposeView
import dev.yeying.ime.bridge.ComposeBridge
import dev.yeying.ime.engine.RimeEngine
import dev.yeying.ime.ui.theme.YeyingTheme

class YeyingImeService : InputMethodService() {

    private val bridge = ComposeBridge()

    override fun onCreate() {
        super.onCreate()
        bridge.onCreate()
        RimeEngine.instance.startup(this)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        bridge.onStart()
        return bridge.createComposeView(composeView) {
            YeyingTheme {
                YeyingKeyboard()
            }
        }
    }

    override fun onDestroy() {
        bridge.onDestroy()
        RimeEngine.instance.shutdown()
        super.onDestroy()
    }
}
