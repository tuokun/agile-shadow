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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.CANDIDATE_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DARK_KEY_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DARK_TOOLBAR_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.DEFAULT_TEXT
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.GlassKeyButton
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KEYCODE_DELETE
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.TOOLBAR_BG
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardAction
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardType
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardViewModel

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
    val textColor = if (isDark) Color.White else DEFAULT_TEXT

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

/** 拼音标签：浮在键盘左上角的独立小块 */
@Composable
internal fun ComposingTag(
    text: String,
    textColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isDark) Color(0x153A3A3E) else Color(0x15D8D8D8)
    val bgColor = if (isDark) Color(0xFF2A2A2E) else Color(0xFFF0F0F2)
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(0.5.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
        )
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
            itemsIndexed(candidates, key = { _, it -> it.text }) { index, candidate ->
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

/** 全屏候选词视图：左拼音 + 中间候选词网格 + 右侧操作栏 */
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
            // 左侧拼音区（仅 T9 拼音模式显示）
            if (!isHandwriting && state.pinyins.isNotEmpty()) {
                val sidebarBg = if (isDark) DARK_TOOLBAR_BG else TOOLBAR_BG
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 3.dp, top = 4.dp, bottom = 5.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(sidebarBg),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
            }

            // 中间候选词网格
            val candidateBg = if (isDark) DARK_KEY_BG else Color.White
            val dividerColor = if (isDark) Color(0xFF484848) else Color(0xFFD0D0D0)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .padding(start = 3.dp, end = 0.dp, top = 4.dp, bottom = 5.dp),
                contentPadding = PaddingValues(top = 3.dp),
            ) {
                itemsIndexed(
                    state.candidates,
                    key = { _, it -> it.text },
                    span = { _, candidate ->
                        val len = candidate.text.length
                        val cols = when {
                            len <= 2 -> 1
                            len <= 5 -> 2
                            else -> 4
                        }
                        GridItemSpan(cols)
                    },
                ) { index, candidate ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(51.dp)
                            .background(candidateBg)
                            .drawBehind {
                                val stroke = 0.5.dp.toPx()
                                drawLine(dividerColor, Offset(0f, size.height), Offset(size.width, size.height), stroke)
                            }
                            .clickable { viewModel.onAction(KeyboardAction.CandidateSelect(index)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = candidate.text,
                            fontSize = 16.sp,
                            color = if (isDark) Color.White else DEFAULT_TEXT,
                        )
                    }
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
                    GlassKeyButton(
                        isDark = isDark,
                        label = "",
                        icon = Icons.AutoMirrored.Outlined.Backspace,
                        onClick = { viewModel.clearHandwritingCandidates() },
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
                    GlassKeyButton(
                        isDark = isDark,
                        label = "返回",
                        onClick = { viewModel.toggleCandidatesExpanded() },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                    GlassKeyButton(
                        isDark = isDark,
                        label = "",
                        icon = Icons.AutoMirrored.Outlined.Backspace,
                        onClick = {
                            viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_DELETE))
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                    GlassKeyButton(
                        isDark = isDark,
                        label = "重输",
                        onClick = {
                            viewModel.onAction(KeyboardAction.ClearComposition)
                            viewModel.toggleCandidatesExpanded()
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        height = 0.dp,
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