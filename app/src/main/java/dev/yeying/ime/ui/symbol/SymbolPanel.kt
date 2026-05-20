package dev.yeying.ime.ui.symbol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.ui.keyboard.KeyboardAction
import dev.yeying.ime.ui.keyboard.KeyboardViewModel
import kotlinx.coroutines.launch

private data class SymbolCategory(
    val label: String,
    val symbols: List<String>,
)

private val symbolCategories = listOf(
    SymbolCategory("常用", listOf(
        "，", "。", "！", "？", "、", "：", "；", """, """, """, """,
        "（", "）", "【", "】", "《", "》", "——", "……", "·", "～",
        "￥", "$", "€", "£", "%", "&", "@", "#", "^", "*",
    )),
    SymbolCategory("中文", listOf(
        "，", "。", "、", "：", "；", "？", "！", """, """, """, """,
        "（", "）", "【", "】", "《", "》", "〈", "〉", "〔", "〕",
        "——", "……", "·", "～", "「", "」", "『", "』", "【", "】",
    )),
    SymbolCategory("英文", listOf(
        ",", ".", "!", "?", ";", ":", "'", "\"", "(", ")",
        "[", "]", "{", "}", "<", ">", "/", "\\", "@", "#",
        "$", "%", "^", "&", "*", "+", "-", "=", "_", "|",
    )),
    SymbolCategory("特殊", listOf(
        "℃", "℉", "°", "±", "×", "÷", "≠", "≈", "≤", "≥",
        "∞", "√", "∑", "∏", "∫", "Δ", "π", "Ω", "μ", "†",
        "§", "№", "※", "☆", "★", "○", "●", "◇", "◆", "□",
    )),
)

@Composable
fun SymbolPanel(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { symbolCategories.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            symbolCategories.forEachIndexed { index, category ->
                Text(
                    text = category.label,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                        .padding(4.dp),
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val category = symbolCategories[page]
            LazyVerticalGrid(
                columns = GridCells.Fixed(10),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(category.symbols) { symbol ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                viewModel.onAction(KeyboardAction.DirectCommit(symbol))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = symbol, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
