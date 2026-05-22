package io.github.cgfhsc.agileshadow.ime.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.material.icons.rounded.KeyboardAlt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.cgfhsc.agileshadow.ime.BuildConfig
import io.github.cgfhsc.agileshadow.ime.data.Prefs
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToOssLicenses: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    val darkTheme by prefs.darkTheme.collectAsState(initial = false)
    val followSystem by prefs.followSystemTheme.collectAsState(initial = false)
    val defaultKeyboard by prefs.defaultKeyboard.collectAsState(initial = KeyboardType.QWERTY)
    val excludeFromRecents by prefs.excludeFromRecents.collectAsState(initial = false)

    var showKeyboardPicker by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor),
    ) {
        TopAppBar(
            title = {
                Text(
                    "曳影输入法",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "返回",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = surfaceColor,
            ),
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // 常规分组
            SectionLabel("常规")
            SettingsGroup {
                SettingItem(
                    icon = Icons.Rounded.KeyboardAlt,
                    title = "默认键盘",
                    subtitle = keyboardDisplayName(defaultKeyboard),
                    onClick = { showKeyboardPicker = true },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.VisibilityOff,
                    title = "后台隐藏",
                    subtitle = "在「最近任务」隐藏卡片",
                    trailing = {
                        Switch(
                            checked = excludeFromRecents,
                            onCheckedChange = { scope.launch { prefs.setExcludeFromRecents(it) } },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    },
                )
            }

            Spacer(Modifier.height(24.dp))

            // 外观分组
            SectionLabel("外观")
            SettingsGroup {
                SettingItem(
                    icon = Icons.Rounded.InvertColors,
                    title = "跟随系统",
                    trailing = {
                        Switch(
                            checked = followSystem,
                            onCheckedChange = { scope.launch { prefs.setFollowSystemTheme(it) } },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "深色模式",
                    enabled = !followSystem,
                    trailing = {
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { scope.launch { prefs.setDarkTheme(it) } },
                            enabled = !followSystem,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    },
                )
            }

            Spacer(Modifier.height(24.dp))

            // 关于分组
            SectionLabel("关于")
            SettingsGroup {
                SettingItem(
                    icon = Icons.Rounded.Info,
                    title = "版本",
                    subtitle = BuildConfig.VERSION_NAME,
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Code,
                    title = "开源声明",
                    trailing = { Chevron() },
                    onClick = onNavigateToOssLicenses,
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Feedback,
                    title = "意见反馈",
                    trailing = {
                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/cgfhsc/agile-shadow/issues"),
                        )
                        context.startActivity(intent)
                    },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showKeyboardPicker) {
        KeyboardPickerBottomSheet(
            currentKeyboard = defaultKeyboard,
            onSelect = { scope.launch { prefs.setDefaultKeyboard(it) } },
            onDismiss = { showKeyboardPicker = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        content()
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (enabled) MaterialTheme.colorScheme.primary else contentColor,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f),
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.Rounded.ChevronRight,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun keyboardDisplayName(type: KeyboardType): String = when (type) {
    KeyboardType.QWERTY -> "QWERTY 全键"
    KeyboardType.T9 -> "T9 九宫格"
    else -> type.name
}
