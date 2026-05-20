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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_CLEAR_MS = 1200L

@Composable
fun HandwritingBoard(
    modifier: Modifier = Modifier,
    onCandidateSelected: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val engine = remember { HandwritingEngine().also { it.init(context) } }
    var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentStroke by remember { mutableStateOf(listOf<Offset>()) }
    var candidates by remember { mutableStateOf(listOf<String>()) }
    var clearJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    fun resetBoard() {
        clearJob?.cancel()
        strokes = emptyList()
        candidates = emptyList()
        engine.clear()
    }

    DisposableEffect(Unit) {
        onDispose {
            clearJob?.cancel()
            engine.release()
        }
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
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                clearJob?.cancel()
                                currentStroke = listOf(offset)
                                engine.addPoint(offset.x.toInt(), offset.y.toInt())
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                                engine.addPoint(change.position.x.toInt(), change.position.y.toInt())
                            },
                            onDragEnd = {
                                strokes = strokes + listOf(currentStroke)
                                currentStroke = emptyList()
                                engine.finishStroke()
                                candidates = engine.recognize()

                                clearJob = scope.launch {
                                    delay(AUTO_CLEAR_MS)
                                    strokes = emptyList()
                                    engine.clear()
                                }
                            },
                        )
                    }
            ) {
                val allStrokes = strokes + if (currentStroke.isNotEmpty()) listOf(currentStroke) else emptyList()
                allStrokes.forEach { strokePoints ->
                    if (strokePoints.size > 1) {
                        val path = Path()
                        path.moveTo(strokePoints.first().x, strokePoints.first().y)
                        for (i in 1 until strokePoints.size) {
                            path.lineTo(strokePoints[i].x, strokePoints[i].y)
                        }
                        drawPath(path, Color.Black, style = Stroke(width = 4f))
                    }
                }
            }

            if (strokes.isEmpty() && currentStroke.isEmpty() && candidates.isEmpty()) {
                Text(
                    text = "在此区域书写",
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
