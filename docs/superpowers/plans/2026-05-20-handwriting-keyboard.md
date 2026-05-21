# 手写键盘布局优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将手写候选词移至顶部 ToolbarCandidateBar，添加右侧标点+删除面板和 T9 一致的底部工具栏。

**Architecture:** HandwritingBoard 重构为三部分布局：画布+右侧面板（Row）、底部工具栏。候选词通过 ViewModel 新增方法注入 state。ViewModel 区分手写候选词，点击时直接 commit 而不走 Rime 引擎。

**Tech Stack:** Jetpack Compose, GlassKeyButton 组件复用

---

### Task 1: KeyboardState + ViewModel 支持手写候选词

**Files:**
- Modify: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardState.kt`
- Modify: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt`

- [ ] **Step 1: KeyboardState 添加手写候选词标记字段**

在 `KeyboardState.kt` 的 data class 中添加 `handwritingCandidates` 字段：

```kotlin
data class KeyboardState(
    val activeKeyboard: KeyboardType = KeyboardType.T9,
    val candidates: List<CandidateListItem> = emptyList(),
    val composingText: String = "",
    val capsState: CapsState = CapsState.NONE,
    val hasNextPage: Boolean = false,
    val page: Int = 0,
    val candidatesExpanded: Boolean = false,
    val pinyins: List<String> = emptyList(),
    val previousKeyboard: KeyboardType = KeyboardType.T9,
    val clipboardSuggestion: String? = null,
    val handwritingCandidates: List<String> = emptyList(),
)
```

- [ ] **Step 2: ViewModel 添加手写候选词方法**

在 `KeyboardViewModel.kt` 的 `setClipboardSuggestion` 方法之后添加两个方法：

```kotlin
fun setHandwritingCandidates(candidates: List<String>) {
    _state.update { s ->
        s.copy(
            candidates = candidates.map { CandidateListItem(it, it) },
            composingText = "",
            handwritingCandidates = candidates,
        )
    }
}

fun clearHandwritingCandidates() {
    _state.update { s ->
        s.copy(candidates = emptyList(), composingText = "", handwritingCandidates = emptyList())
    }
}
```

- [ ] **Step 3: 修改 handleCandidateSelect 区分手写候选词**

在 `KeyboardViewModel.kt` 中修改 `handleCandidateSelect` 方法：

```kotlin
    private fun handleCandidateSelect(action: KeyboardAction.CandidateSelect) {
        val hwCandidates = _state.value.handwritingCandidates
        if (hwCandidates.isNotEmpty()) {
            val text = hwCandidates.getOrElse(action.index) { null }
            if (text != null) {
                onCommitText(text)
                clearHandwritingCandidates()
            }
            return
        }

        RimeEngine.instance.selectCandidate(action.index)
        val committed = RimeEngine.instance.commitIfNeeded()
        if (committed != null) {
            onCommitText(committed)
            inputKeyTracker.clear()
        }
        accumulatedCandidates.clear()
        refreshState()
    }
```

- [ ] **Step 4: 构建验证**

Run: `cd /Users/cgfhsc/dev/project/github/ime/yeying && ./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

---

### Task 2: 重构 HandwritingBoard 布局

**Files:**
- Modify: `app/src/main/java/dev/yeying/ime/ui/keyboard/HandwritingBoard.kt`

这是主要改动。HandwritingBoard 签名和内部布局全部重写。

- [ ] **Step 1: 用完整新内容替换 HandwritingBoard.kt**

将整个 `HandwritingBoard.kt` 文件替换为以下内容：

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.engine.HandwritingEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_CLEAR_MS = 1200L

@Composable
fun HandwritingBoard(
    modifier: Modifier = Modifier,
    viewModel: KeyboardViewModel,
    onCommitText: (String) -> Unit = {},
    onDeleteChar: () -> Unit = {},
) {
    val context = LocalContext.current
    val engine = remember { HandwritingEngine() }
    val scope = rememberCoroutineScope()

    var initResult by remember { mutableStateOf(false to "初始化中...") }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(Unit) {
        val success = engine.init(context)
        initResult = success to if (success) "" else "初始化失败，请检查网络后重试"
    }

    DisposableEffect(Unit) {
        onDispose {
            engine.release()
            viewModel.clearHandwritingCandidates()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 画布 + 右侧面板
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 手写画布
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .pointerInput(engine.isInitialized) {
                            if (!engine.isInitialized) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke.clear()
                                    currentStroke.add(offset)
                                    engine.addPoint(offset.x.toInt(), offset.y.toInt())
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStroke.add(change.position)
                                    engine.addPoint(change.position.x.toInt(), change.position.y.toInt())
                                },
                                onDragEnd = {
                                    strokes.addAll(listOf(currentStroke.toList()))
                                    currentStroke.clear()
                                    engine.finishStroke()

                                    scope.launch {
                                        val result = engine.recognize()
                                        viewModel.setHandwritingCandidates(result)
                                        delay(AUTO_CLEAR_MS)
                                        strokes.clear()
                                        engine.clear()
                                    }
                                },
                            )
                        }
                ) {
                    for (strokePoints in strokes) {
                        drawStrokePath(strokePoints)
                    }
                    if (currentStroke.isNotEmpty()) {
                        drawStrokePath(currentStroke.toList())
                    }
                }

                if (strokes.isEmpty() && currentStroke.isEmpty()) {
                    val hintText = if (!initResult.first) initResult.second else "在此区域书写"
                    Text(
                        text = hintText,
                        fontSize = 16.sp,
                        color = Color.Gray.copy(alpha = 0.5f),
                    )
                }
            }

            // 右侧面板：标点 + 删除
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                GlassKeyButton(
                    label = "，",
                    onClick = { onCommitText("，") },
                    modifier = Modifier.weight(1f),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "。",
                    onClick = { onCommitText("。") },
                    modifier = Modifier.weight(1f),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "？",
                    onClick = { onCommitText("？") },
                    modifier = Modifier.weight(0.85f),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "！",
                    onClick = { onCommitText("！") },
                    modifier = Modifier.weight(0.85f),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "",
                    icon = Icons.AutoMirrored.Outlined.Backspace,
                    onClick = { onDeleteChar() },
                    modifier = Modifier.weight(1.1f),
                    height = 0.dp,
                )
            }
        }

        // 底部工具栏（与 T9 一致：左1 + 中4 + 右1）
        val state by viewModel.state.collectAsState()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassKeyButton(
                label = "符号",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_SYMBOL)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
            )
            Row(
                modifier = Modifier.weight(4f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                GlassKeyButton(
                    label = "123",
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_NUMBER)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
                GlassKeyButton(
                    label = "",
                    icon = Icons.Outlined.SpaceBar,
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_SPACE)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
                GlassKeyButton(
                    label = "",
                    icon = Icons.Outlined.Language,
                    subLabel = if (state.activeKeyboard == KeyboardType.ENGLISH) "EN" else "中",
                    onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_SWITCH_LANG)) },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
            }
            GlassKeyButton(
                label = "确认",
                onClick = { viewModel.onAction(KeyboardAction.KeyPress(KEYCODE_ENTER)) },
                modifier = Modifier.weight(1f),
                height = 44.dp,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokePath(points: List<Offset>) {
    if (points.size < 2) return
    val path = Path()
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }
    drawPath(path, Color.Black, style = Stroke(width = 4f))
}
```

- [ ] **Step 2: 构建验证**

Run: `cd /Users/cgfhsc/dev/project/github/ime/yeying && ./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

---

### Task 3: 更新 YeyingKeyboard 调用

**Files:**
- Modify: `app/src/main/java/dev/yeying/ime/YeyingKeyboard.kt`

- [ ] **Step 1: 更新 HandwritingBoard 调用处**

在 `YeyingKeyboard.kt` 第 63 行附近，替换：

```kotlin
                KeyboardType.HANDWRITING -> HandwritingBoard(
                    onCandidateSelected = onCommitText,
                )
```

改为：

```kotlin
                KeyboardType.HANDWRITING -> HandwritingBoard(
                    viewModel = viewModel,
                    onCommitText = onCommitText,
                    onDeleteChar = onDeleteChar,
                )
```

- [ ] **Step 2: 构建验证**

Run: `cd /Users/cgfhsc/dev/project/github/ime/yeying && ./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

---

### Task 4: 端到端验证

- [ ] **Step 1: 安装到设备并验证**

Run: `cd /Users/cgfhsc/dev/project/github/ime/yeying && ./gradlew installDebug`

验证清单：
1. 切换到手写键盘，确认布局正确：画布 + 右侧标点面板 + 底部工具栏
2. 手写一个字，确认候选词出现在顶部 ToolbarCandidateBar
3. 点击候选词，确认正确上屏
4. 点击右侧标点（，。？！），确认直接上屏
5. 点击右侧删除，确认删除前一个字符
6. 底部栏：符号、123、空格、中/英切换、确认按钮全部正常
7. 切换回 T9 键盘，确认底部栏结构一致
