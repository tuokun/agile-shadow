package dev.yeying.ime.ui.tools

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import dev.yeying.ime.data.clipboard.ClipboardDatabase
import dev.yeying.ime.ui.keyboard.GlassKeyButton
import dev.yeying.ime.ui.keyboard.KeyboardAction
import dev.yeying.ime.ui.keyboard.KeyboardType
import dev.yeying.ime.ui.keyboard.KeyboardViewModel

@Composable
fun ToolsPanel(
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit = {},
    onSendKey: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        // 工具按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GlassKeyButton(
                label = "😀",
                subLabel = "表情",
                onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.EMOJI)) },
                modifier = Modifier.weight(1f),
                height = 52.dp,
            )
            GlassKeyButton(
                label = "📋",
                subLabel = "剪切板",
                onClick = { /* scroll to clipboard section below */ },
                modifier = Modifier.weight(1f),
                height = 52.dp,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 编辑工具
        val editTools = listOf(
            "全选" to android.R.id.selectAll,
            "剪切" to android.R.id.cut,
            "复制" to android.R.id.copy,
            "粘贴" to android.R.id.paste,
            "←" to KeyEvent.KEYCODE_DPAD_LEFT,
            "→" to KeyEvent.KEYCODE_DPAD_RIGHT,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.fillMaxWidth().height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(editTools) { (label, actionId) ->
                GlassKeyButton(
                    label = label,
                    onClick = { onSendKey(actionId) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 40.dp,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 剪贴板
        ClipboardSection(onCommitText)

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ClipboardSection(
    onPaste: (String) -> Unit,
) {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(context, ClipboardDatabase::class.java, "clipboard.db")
            .build()
    }
    val dao = db.clipboardDao()
    val items by dao.getAll().collectAsState(initial = emptyList())

    if (items.isEmpty()) return

    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(items, key = { it.id }) { item ->
            GlassKeyButton(
                label = item.text,
                onClick = { onPaste(item.text) },
                modifier = Modifier.fillMaxWidth(),
                height = 32.dp,
            )
        }
    }
}
