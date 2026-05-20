package dev.yeying.ime.ui.tools

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.yeying.ime.ui.keyboard.GlassKeyButton
import dev.yeying.ime.ui.keyboard.KeyboardAction
import dev.yeying.ime.ui.keyboard.KeyboardType
import dev.yeying.ime.ui.keyboard.KeyboardViewModel
import dev.yeying.ime.ui.settings.SettingsActivity

private data class ToolItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: () -> Unit,
)

@Composable
fun ToolsPanel(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val tools = listOf(
        ToolItem("表情", Icons.Outlined.EmojiEmotions) {
            viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.EMOJI))
        },
        ToolItem("键盘", Icons.Outlined.Keyboard) {
            viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.KEYBOARD_PICKER))
        },
        ToolItem("剪贴板", Icons.Outlined.ContentPaste) {
            viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.CLIPBOARD))
        },
        ToolItem("编辑", Icons.Outlined.EditNote) {
            viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.EDIT))
        },
        ToolItem("符号", Icons.Outlined.ShortText) {
            viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.SYMBOL))
        },
        ToolItem("数字", Icons.Outlined.Numbers) {
            viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.NUMBER))
        },
        ToolItem("设置", Icons.Outlined.Settings) {
            context.startActivity(Intent(context, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        },
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(tools) { item ->
            GlassKeyButton(
                label = item.label,
                subLabel = item.label,
                icon = item.icon,
                onClick = item.action,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp,
            )
        }
    }
}
