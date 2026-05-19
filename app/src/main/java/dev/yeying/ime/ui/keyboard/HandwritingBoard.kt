package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun HandwritingBoard(
    modifier: Modifier = Modifier,
    onCandidateSelected: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val engine = remember { HandwritingEngine().also { it.init(context) } }
    var paths by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember { mutableStateOf(listOf<Offset>()) }
    var candidates by remember { mutableStateOf(listOf<List<String>>()) }
    val points = remember { mutableListOf<Int>() }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = listOf(offset)
                            points.clear()
                            points.add(offset.x.toInt())
                            points.add(offset.y.toInt())
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath = currentPath + change.position
                            points.add(change.position.x.toInt())
                            points.add(change.position.y.toInt())
                        },
                        onDragEnd = {
                            paths = paths + currentPath
                            currentPath = emptyList()
                            val result = engine.recognize(points.toIntArray())
                            candidates = result
                            paths = emptyList()
                        },
                    )
                }
        ) {
            val allPaths = paths + if (currentPath.isNotEmpty()) listOf(currentPath) else emptyList()
            allPaths.forEach { pathPoints ->
                if (pathPoints.size > 1) {
                    val path = Path()
                    path.moveTo(pathPoints.first().x, pathPoints.first().y)
                    for (i in 1 until pathPoints.size) {
                        path.lineTo(pathPoints[i].x, pathPoints[i].y)
                    }
                    drawPath(path, Color.Black, style = Stroke(width = 3f))
                }
            }
        }

        if (candidates.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                candidates.first().forEach { candidate ->
                    Text(
                        text = candidate,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}
