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
import androidx.compose.foundation.layout.padding
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

private const val AUTO_CLEAR_MS = 2500L

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

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 画布 + 右侧面板
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp),
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

            // 右侧面板：标点 + 删除（宽度按比例匹配确认键）
            Column(
                modifier = Modifier.weight(0.2f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                GlassKeyButton(
                    label = "，",
                    onClick = { onCommitText("，") },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "。",
                    onClick = { onCommitText("。") },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "？",
                    onClick = { onCommitText("？") },
                    modifier = Modifier.weight(0.85f).fillMaxWidth(),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "！",
                    onClick = { onCommitText("！") },
                    modifier = Modifier.weight(0.85f).fillMaxWidth(),
                    height = 0.dp,
                )
                GlassKeyButton(
                    label = "",
                    icon = Icons.AutoMirrored.Outlined.Backspace,
                    onClick = {
                        if (viewModel.state.value.handwritingCandidates.isNotEmpty()) {
                            viewModel.clearHandwritingCandidates()
                        } else {
                            onDeleteChar()
                        }
                    },
                    modifier = Modifier.weight(1.1f).fillMaxWidth(),
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
    drawPath(path, Color.Black, style = Stroke(width = 8f))
}
