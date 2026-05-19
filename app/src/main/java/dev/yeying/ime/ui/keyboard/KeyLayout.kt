package dev.yeying.ime.ui.keyboard

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyDef(
    val label: String,
    val code: Int,
    val width: Dp = 0.dp,
    val isRepeatable: Boolean = false,
)

const val KEYCODE_SHIFT = -1
const val KEYCODE_DELETE = -2
const val KEYCODE_SYMBOL = -3

object QwertyLayout {

    val row1 = listOf(
        KeyDef("q", 'q'.code), KeyDef("w", 'w'.code),
        KeyDef("e", 'e'.code), KeyDef("r", 'r'.code),
        KeyDef("t", 't'.code), KeyDef("y", 'y'.code),
        KeyDef("u", 'u'.code), KeyDef("i", 'i'.code),
        KeyDef("o", 'o'.code), KeyDef("p", 'p'.code),
    )

    val row2 = listOf(
        KeyDef("a", 'a'.code), KeyDef("s", 's'.code),
        KeyDef("d", 'd'.code), KeyDef("f", 'f'.code),
        KeyDef("g", 'g'.code), KeyDef("h", 'h'.code),
        KeyDef("j", 'j'.code), KeyDef("k", 'k'.code),
        KeyDef("l", 'l'.code),
    )

    val row3 = listOf(
        KeyDef("⇧", KEYCODE_SHIFT),
        KeyDef("z", 'z'.code), KeyDef("x", 'x'.code),
        KeyDef("c", 'c'.code), KeyDef("v", 'v'.code),
        KeyDef("b", 'b'.code), KeyDef("n", 'n'.code),
        KeyDef("m", 'm'.code),
        KeyDef("⌫", KEYCODE_DELETE, isRepeatable = true),
    )

    val row4 = listOf(
        KeyDef("?123", KEYCODE_SYMBOL),
        KeyDef(",", ','.code, width = 40.dp),
        KeyDef("空格", ' '.code),
        KeyDef(".", '.'.code, width = 40.dp),
        KeyDef("↵", '\n'.code),
    )

    val rows = listOf(row1, row2, row3, row4)
}
