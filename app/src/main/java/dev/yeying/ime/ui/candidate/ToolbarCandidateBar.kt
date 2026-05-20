package dev.yeying.ime.ui.candidate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.ui.keyboard.GlassKeyButton
import dev.yeying.ime.ui.keyboard.KEYCODE_DELETE
import dev.yeying.ime.ui.keyboard.KeyboardAction
import dev.yeying.ime.ui.keyboard.KeyboardType
import dev.yeying.ime.ui.keyboard.KeyboardViewModel

@Composable
fun ToolbarCandidateBar(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    onHideKeyboard: () -> Unit = {},
    onCommitText: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    val suggestion = state.clipboardSuggestion

    if (state.composingText.isNotEmpty() || state.candidates.isNotEmpty()) {
        CandidateRow(viewModel, state.candidates, state.hasNextPage, state.candidatesExpanded, modifier)
    } else if (suggestion != null) {
        ClipboardSuggestionRow(
            text = suggestion,
            onClick = {
                onCommitText(suggestion)
                viewModel.setClipboardSuggestion(null)
            },
            onDismiss = { viewModel.setClipboardSuggestion(null) },
            modifier = modifier,
        )
    } else {
        ToolbarRow(viewModel, modifier, onHideKeyboard)
    }
}

/** 单行候选词：LazyRow 累积滚动 + 滑到底自动加载 */
@Composable
private fun CandidateRow(
    viewModel: KeyboardViewModel,
    candidates: List<com.yuyan.inputmethod.core.CandidateListItem>,
    hasNextPage: Boolean,
    candidatesExpanded: Boolean,
    modifier: Modifier,
) {
    val listState = rememberLazyListState()

    val reachedEnd by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 1
        }
    }
    LaunchedEffect(reachedEnd, hasNextPage, candidates.size) {
        if (reachedEnd && hasNextPage) viewModel.nextPage()
    }

    var prevCount by remember { mutableStateOf(0) }
    LaunchedEffect(candidates.size) {
        if (candidates.size < prevCount) listState.scrollToItem(0)
        prevCount = candidates.size
    }

    Row(
        modifier = modifier.fillMaxWidth().height(50.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(candidates) { index, candidate ->
                Text(
                    text = candidate.text,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        viewModel.onAction(KeyboardAction.CandidateSelect(index))
                    },
                )
            }
        }
        Text(
            text = if (candidatesExpanded) "△" else "▽",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { viewModel.toggleCandidatesExpanded() }
                .padding(horizontal = 6.dp),
        )
    }
}

/** 全屏候选词视图：替换整个键盘区域，Flexbox 网格 + 懒加载 + 删除键 */
@Composable
fun ExpandedCandidateView(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()

    // 滑到底自动加载
    val reachedEnd by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 5
        }
    }
    LaunchedEffect(reachedEnd, state.hasNextPage) {
        if (reachedEnd && state.hasNextPage) viewModel.nextPage()
    }

    Box(modifier = modifier.fillMaxWidth().height(248.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(state.candidates, key = { _, it -> it.text }) { index, candidate ->
                Text(
                    text = candidate.text,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable {
                            viewModel.onAction(KeyboardAction.CandidateSelect(index))
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                )
            }
        }

        // 右下角：删除键
        Text(
            text = "⌫",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable {
                    viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_DELETE))
                }
                .padding(12.dp),
        )

    }
}

@Composable
private fun ToolbarRow(
    viewModel: KeyboardViewModel,
    modifier: Modifier,
    onHideKeyboard: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth().height(50.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 工具面板
        val toolsTarget = if (state.activeKeyboard == KeyboardType.TOOLS) state.previousKeyboard else KeyboardType.TOOLS
        GlassKeyButton(
            label = "工具",
            icon = Icons.Outlined.Menu,
            onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(toolsTarget)) },
            modifier = Modifier.weight(1f),
            height = 36.dp,
        )

        // 键盘方案选择
        val pickerTarget = if (state.activeKeyboard == KeyboardType.KEYBOARD_PICKER) state.previousKeyboard else KeyboardType.KEYBOARD_PICKER
        GlassKeyButton(
            label = "键盘",
            icon = Icons.Outlined.Keyboard,
            onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(pickerTarget)) },
            modifier = Modifier.weight(1f),
            height = 36.dp,
        )

        // 表情
        val emojiTarget = if (state.activeKeyboard == KeyboardType.EMOJI) state.previousKeyboard else KeyboardType.EMOJI
        GlassKeyButton(
            label = "表情",
            icon = Icons.Outlined.EmojiEmotions,
            onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(emojiTarget)) },
            modifier = Modifier.weight(1f),
            height = 36.dp,
        )

        // 编辑
        val editTarget = if (state.activeKeyboard == KeyboardType.EDIT) state.previousKeyboard else KeyboardType.EDIT
        GlassKeyButton(
            label = "编辑",
            icon = Icons.Outlined.Edit,
            onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(editTarget)) },
            modifier = Modifier.weight(1f),
            height = 36.dp,
        )

        // 剪贴板
        val clipboardTarget = if (state.activeKeyboard == KeyboardType.CLIPBOARD) state.previousKeyboard else KeyboardType.CLIPBOARD
        GlassKeyButton(
            label = "剪贴板",
            icon = Icons.Outlined.ContentPaste,
            onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(clipboardTarget)) },
            modifier = Modifier.weight(1f),
            height = 36.dp,
        )

        // 收起键盘
        GlassKeyButton(
            label = "收起",
            icon = Icons.Outlined.KeyboardArrowDown,
            onClick = onHideKeyboard,
            modifier = Modifier.weight(1f),
            height = 36.dp,
        )
    }
}

@Composable
private fun ClipboardSuggestionRow(
    text: String,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(50.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
        )
        Text(
            text = "✕",
            fontSize = 16.sp,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(horizontal = 8.dp),
        )
    }
}