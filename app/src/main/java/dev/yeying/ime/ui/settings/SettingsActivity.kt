package dev.yeying.ime.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.yeying.ime.ui.theme.YeyingTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YeyingTheme {
                SettingsScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
