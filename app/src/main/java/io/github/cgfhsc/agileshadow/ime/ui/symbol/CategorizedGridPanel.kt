package io.github.cgfhsc.agileshadow.ime.ui.symbol

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.GlassKeyButton
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KEYBOARD_BOTTOM_SPACER
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.TOOLBAR_BG

data class SymbolGridCategory(
    val label: String,
    val items: List<String>,
)

private val SIDEBAR_BG = Color(0xFFE4E7EB)
private val DARK_SIDEBAR_BG = Color(0xFF252628)
private val ACCENT = Color(0xFF3482FF)
private val DARK_ACCENT = Color(0xFF5BA3FF)

@Composable
internal fun CategorizedGridPanel(
    categories: List<SymbolGridCategory>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    columns: Int = 4,
    onBack: (() -> Unit)? = null,
    isLocked: Boolean = false,
    onLockToggle: (() -> Unit)? = null,
) {
    val (selectedIndex, setSelectedIndex) = remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f)) {
        // Left sidebar
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            // Merged scrollable category container
            val sidebarBg = if (isDark) DARK_SIDEBAR_BG else SIDEBAR_BG
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 3.dp, end = 3.dp, top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(sidebarBg),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 3.dp),
                ) {
                    categories.forEachIndexed { index, category ->
                        item(key = category.label) {
                            val selected = index == selectedIndex
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clickable { setSelectedIndex(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .fillMaxHeight()
                                            .width(3.dp)
                                            .background(
                                                if (isDark) DARK_ACCENT else ACCENT,
                                                RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp),
                                            ),
                                    )
                                }
                                GlassKeyButton(
                                    isDark = isDark,
                                    label = category.label,
                                    onClick = { setSelectedIndex(index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    height = 0.dp,
                                    isActive = selected,
                                    showBorder = false,
                                    keyBackgroundColor = if (selected) {
                                        if (isDark) Color(0xFF323335) else Color.White
                                    } else {
                                        sidebarBg
                                    },
                                    textColor = if (selected) {
                                        if (isDark) DARK_ACCENT else ACCENT
                                    } else Color.Unspecified,
                                )
                            }
                        }
                    }
                }
            }

            // Bottom actions: lock + back
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                onLockToggle?.let {
                    GlassKeyButton(
                        isDark = isDark,
                        label = "",
                        icon = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        onClick = onLockToggle,
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                        iconSize = 20.dp,
                    )
                }
                onBack?.let {
                    GlassKeyButton(
                        isDark = isDark,
                        label = "返回",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        keyBackgroundColor = TOOLBAR_BG,
                    )
                }
            }

        }

        // Right grid
        val category = categories[selectedIndex]
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(5f)
                .fillMaxHeight()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            items(category.items) { item ->
                GlassKeyButton(
                    isDark = isDark,
                    label = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 49.dp,
                    showBorder = false,
                )
            }
        }
        }

        Spacer(modifier = Modifier.height(44.dp))
    }
}
