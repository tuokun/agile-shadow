package dev.yeying.ime.ui.keyboard

object T9Layout {
    data class T9Key(
        val label: String,
        val subLabel: String = "",
        val code: Int,
    )

    val row1 = listOf(
        T9Key("1", "符号", KEYCODE_SYMBOL),
        T9Key("'", "'", KEYCODE_QUOTE),
        T9Key("ABC", "2", KEYCODE_T9_2),
        T9Key("DEF", "3", KEYCODE_T9_3),
        T9Key("⌫", "删除", KEYCODE_DELETE),
    )

    val row2 = listOf(
        T9Key("GHI", "4", KEYCODE_T9_4),
        T9Key("JKL", "5", KEYCODE_T9_5),
        T9Key("MNO", "6", KEYCODE_T9_6),
        T9Key("清除", "", KEYCODE_CLEAR),
    )

    val row3 = listOf(
        T9Key("PQRS", "7", KEYCODE_T9_7),
        T9Key("TUV", "8", KEYCODE_T9_8),
        T9Key("WXYZ", "9", KEYCODE_T9_9),
        T9Key("⇧", "", KEYCODE_SHIFT),
    )

    val row4 = listOf(
        T9Key("?123", "", KEYCODE_SYMBOL),
        T9Key("，", "", KEYCODE_COMMA),
        T9Key("空格", "", ' '.code),
        T9Key("。", "", KEYCODE_PERIOD),
        T9Key("↵", "", KEYCODE_ENTER),
    )

    val rows = listOf(row1, row2, row3, row4)
}

const val KEYCODE_T9_2 = 'A'.code
const val KEYCODE_T9_3 = 'D'.code
const val KEYCODE_T9_4 = 'G'.code
const val KEYCODE_T9_5 = 'J'.code
const val KEYCODE_T9_6 = 'M'.code
const val KEYCODE_T9_7 = 'P'.code
const val KEYCODE_T9_8 = 'T'.code
const val KEYCODE_T9_9 = 'W'.code
const val KEYCODE_QUOTE = '"'.code
const val KEYCODE_CLEAR = -10
const val KEYCODE_COMMA = ','.code
const val KEYCODE_PERIOD = '.'.code
const val KEYCODE_ENTER = '\n'.code
