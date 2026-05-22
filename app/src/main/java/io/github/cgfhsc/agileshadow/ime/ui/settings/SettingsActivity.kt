package io.github.cgfhsc.agileshadow.ime.ui.settings

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import io.github.cgfhsc.agileshadow.ime.data.Prefs
import io.github.cgfhsc.agileshadow.ime.ui.theme.AgileShadowTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsContent(onBack = { finish() })
        }
    }
}

@Composable
fun SettingsContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    val darkTheme by prefs.darkTheme.collectAsState(initial = false)
    val followSystem by prefs.followSystemTheme.collectAsState(initial = false)

    val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    val isDark = if (followSystem) isSystemDark else darkTheme

    var showOssLicenses by remember { mutableStateOf(false) }

    BackHandler(enabled = showOssLicenses) {
        showOssLicenses = false
    }

    val activity = LocalContext.current as? ComponentActivity
    LaunchedEffect(Unit) {
        prefs.excludeFromRecents.collect { exclude ->
            activity?.let { act ->
                val am = act.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.appTasks.forEach { it.setExcludeFromRecents(exclude) }
            }
        }
    }

    AgileShadowTheme(isDark = isDark) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (showOssLicenses) {
                OssLicensesScreen(onBack = { showOssLicenses = false })
            } else {
                SettingsScreen(
                    onBack = onBack,
                    onNavigateToOssLicenses = { showOssLicenses = true },
                )
            }
        }
    }
}
