package io.github.cgfhsc.agileshadow.ime.ui.candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.CANDIDATE_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.GlassKeyButton
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KEYBOARD_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DARK_KEYBOARD_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DEFAULT_TEXT
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KEYCODE_DELETE
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardAction
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardType
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardViewModel
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.TOOLBAR_BG

@Composable
fun ToolbarCandidateBar(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    onHideKeyboard: () -> Unit = {},
    onCommitText: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    val suggestion = state.clipboardSuggestion
    val textColor = if (isDark) Color.White else Color(0xFF333333)

    if (state.composingText.isNotEmpty() || state.candidates.isNotEmpty()) {
        CandidateRow(viewModel, state.candidates, state.hasNextPage, state.candidatesExpanded, isDark, textColor, modifier)
    } else if (suggestion != null) {
        ClipboardSuggestionRow(
            text = suggestion,
            textColor = textColor,
            onClick = {
                onCommitText(suggestion)
                viewModel.setClipboardSuggestion(null)
            },
            onDismiss = { viewModel.setClipboardSuggestion(null) },
            modifier = modifier,
        )
    } else {
        ToolbarRow(viewModel, state, isDark, modifier, onHideKeyboard)
    }
}

/** 单行候选词：LazyRow 累积滚动 + 滑到底自动加载 */
@Composable
private fun CandidateRow(
    viewModel: KeyboardViewModel,
    candidates: List<com.yuyan.inputmethod.core.CandidateListItem>,
    hasNextPage: Boolean,
    candidatesExpanded: Boolean,
    isDark: Boolean,
    textColor: Color,
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(candidates) { index, candidate ->
                Text(
                    text = candidate.text,
                    fontSize = 18.sp,
                    color = textColor,
                    modifier = Modifier.clickable {
                        viewModel.onAction(KeyboardAction.CandidateSelect(index))
                    },
                )
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { viewModel.toggleCandidatesExpanded() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (candidatesExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowRight,
                contentDescription = if (candidatesExpanded) "收起" else "展开",
                tint = textColor,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/** 全屏候选词视图：左侧候选词网格 + 右侧操作栏 */
@Composable
fun ExpandedCandidateView(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    isHandwriting: Boolean = false,
) {
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()

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

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f)) {
            // 左侧候选词网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                state = gridState,
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                itemsIndexed(
                    state.candidates,
                    key = { _, it -> it.text },
                    span = { _, candidate ->
                        val len = candidate.text.length
                        val cols = when {
                            len <= 2 -> 1
                            len <= 5 -> 2
                            else -> 5
                        }
                        GridItemSpan(cols)
                    },
                ) { index, candidate ->
                    GlassKeyButton(
                        isDark = isDark,
                        label = candidate.text,
                        onClick = { viewModel.onAction(KeyboardAction.CandidateSelect(index)) },
                        modifier = Modifier.fillMaxWidth(),
                        height = 49.dp,
                        showBorder = false,
                    )
                }
            }

            // 右侧操作栏
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 3.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                if (isHandwriting) {
                    // 手写模式：三个按钮均分高度
                    GlassKeyButton(
                        isDark = isDark,
                        label = "",
                        icon = Icons.AutoMirrored.Outlined.Backspace,
                        onClick = {
                            viewModel.clearHandwritingCandidates()
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                    GlassKeyButton(
                        isDark = isDark,
                        label = "重输",
                        onClick = {
                            viewModel.clearHandwritingCandidates()
                            viewModel.toggleCandidatesExpanded()
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                    GlassKeyButton(
                        isDark = isDark,
                        label = "返回",
                        onClick = { viewModel.toggleCandidatesExpanded() },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                } else {
                    // 拼音模式：拼音候选滚动区 + 删除 + 返回
                    if (state.pinyins.isNotEmpty()) {
                        val sidebarBg = if (isDark) Color(0xFF252628) else Color(0xFFE4E7EB)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(sidebarBg),
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                contentPadding = PaddingValues(vertical = 3.dp),
                            ) {
                                itemsIndexed(state.pinyins) { index, pinyin ->
                                    GlassKeyButton(
                                        isDark = isDark,
                                        label = pinyin,
                                        onClick = { viewModel.onAction(KeyboardAction.SelectPinyin(index)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp),
                                        height = 38.dp,
                                        showBorder = false,
                                        keyBackgroundColor = sidebarBg,
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    GlassKeyButton(
                        isDark = isDark,
                        label = "",
                        icon = Icons.AutoMirrored.Outlined.Backspace,
                        onClick = {
                            viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_DELETE))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                    GlassKeyButton(
                        isDark = isDark,
                        label = "返回",
                        onClick = { viewModel.toggleCandidatesExpanded() },
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }
}

@Composable
private fun ToolbarRow(
    viewModel: KeyboardViewModel,
    state: io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardState,
    isDark: Boolean,
    modifier: Modifier,
    onHideKeyboard: () -> Unit,
) {
    val toolbarItems = listOf(
        Icons.Outlined.Menu to KeyboardType.TOOLS,
        Icons.Outlined.Keyboard to KeyboardType.KEYBOARD_PICKER,
        Icons.Outlined.EmojiEmotions to KeyboardType.EMOJI,
        Icons.Outlined.Edit to KeyboardType.EDIT,
        Icons.Outlined.ContentPaste to KeyboardType.CLIPBOARD,
    )

    Row(
        modifier = modifier.fillMaxWidth().height(50.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        toolbarItems.forEach { (icon, type) ->
            val target = if (state.activeKeyboard == type) state.previousKeyboard else type
            GlassKeyButton(
                isDark = isDark,
                label = "",
                icon = icon,
                onClick = { viewModel.onAction(KeyboardAction.SwitchKeyboard(target)) },
                modifier = Modifier.weight(1f),
                height = 36.dp,
                keyBackgroundColor = CANDIDATE_BG,
                showBorder = false,
                iconSize = 24.dp,
                textColor = if (isDark) Color(0xFF9FA1A1) else DEFAULT_TEXT,
            )
        }

        GlassKeyButton(
            isDark = isDark,
            label = "",
            icon = Icons.Outlined.KeyboardArrowDown,
            onClick = onHideKeyboard,
            modifier = Modifier.weight(1f),
            height = 36.dp,
            keyBackgroundColor = CANDIDATE_BG,
            showBorder = false,
            iconSize = 24.dp,
            textColor = if (isDark) Color(0xFF9FA1A1) else DEFAULT_TEXT,
        )
    }
}

@Composable
private fun ClipboardSuggestionRow(
    text: String,
    textColor: Color,
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
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
        )
        Text(
            text = "✕",
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(horizontal = 8.dp),
        )
    }
}