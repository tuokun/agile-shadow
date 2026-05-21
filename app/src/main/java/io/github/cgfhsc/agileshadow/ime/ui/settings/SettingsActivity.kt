package io.github.cgfhsc.agileshadow.ime.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.cgfhsc.agileshadow.ime.ui.theme.AgileShadowTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgileShadowTheme {
                SettingsScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
