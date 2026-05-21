package io.github.cgfhsc.agileshadow.ime.ui.symbol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.GlassKeyButton
import kotlinx.coroutines.launch

data class SymbolGridCategory(
    val label: String,
    val items: List<String>,
)

@Composable
internal fun CategorizedGridPanel(
    categories: List<SymbolGridCategory>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            categories.forEachIndexed { index, category ->
                Text(
                    text = category.label,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White else Color(0xFF333333),
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
            val category = categories[page]
            LazyVerticalGrid(
                columns = GridCells.Fixed(10),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(category.items) { item ->
                    GlassKeyButton(
                        isDark = isDark,
                        label = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        height = 32.dp,
                        showBorder = false,
                    )
                }
            }
        }
    }
}
