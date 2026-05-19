package dev.yeying.ime.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.data.Prefs
import dev.yeying.ime.ui.keyboard.KeyboardType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    val vibration by prefs.keyVibration.collectAsState(initial = true)
    val sound by prefs.keySound.collectAsState(initial = false)
    val darkTheme by prefs.darkTheme.collectAsState(initial = false)
    val defaultKeyboard by prefs.defaultKeyboard.collectAsState(initial = KeyboardType.QWERTY)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        TopAppBar(
            title = { Text("曳影输入法设置") },
        )

        // 键盘设置
        SectionHeader("键盘")
        KeyboardType.entries.forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.launch { prefs.setDefaultKeyboard(type) } }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(type.name)
                if (type == defaultKeyboard) {
                    Text("✓", fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 反馈设置
        SectionHeader("反馈")
        SettingSwitch("按键振动", vibration) { scope.launch { prefs.setKeyVibration(it) } }
        SettingSwitch("按键声音", sound) { scope.launch { prefs.setKeySound(it) } }

        Spacer(modifier = Modifier.height(8.dp))

        // 主题
        SectionHeader("主题")
        SettingSwitch("深色主题", darkTheme) { scope.launch { prefs.setDarkTheme(it) } }

        Spacer(modifier = Modifier.height(8.dp))

        // 关于
        SectionHeader("关于")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("版本 0.1.0")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
