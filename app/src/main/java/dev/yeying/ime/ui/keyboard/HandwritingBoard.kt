package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
    onCandidateSelected: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val engine = remember { HandwritingEngine() }
    val scope = rememberCoroutineScope()

    var initResult by remember { mutableStateOf(false to "初始化中...") }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }
    var candidates by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        val success = engine.init(context)
        initResult = success to if (success) "" else "初始化失败，请检查网络后重试"
    }

    fun resetBoard() {
        strokes.clear()
        candidates = emptyList()
        engine.clear()
    }

    DisposableEffect(Unit) {
        onDispose { engine.release() }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                                    candidates = engine.recognize()
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

            if (strokes.isEmpty() && currentStroke.isEmpty() && candidates.isEmpty()) {
                val hintText = if (!initResult.first) initResult.second else "在此区域书写"
                Text(
                    text = hintText,
                    fontSize = 16.sp,
                    color = Color.Gray.copy(alpha = 0.5f),
                )
            }
        }

        if (candidates.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                candidates.take(8).forEach { candidate ->
                    GlassKeyButton(
                        label = candidate,
                        onClick = {
                            onCandidateSelected(candidate)
                            resetBoard()
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                    )
                }
            }
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
