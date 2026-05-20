package dev.yeying.ime.ui.keyboard

object T9Layout {
    data class T9Key(
        val label: String,
        val subLabel: String = "",
        val code: Int,
    )

    val leftPunctuation = listOf(
        T9Key("，", code = KEYCODE_COMMA),
        T9Key("。", code = KEYCODE_PERIOD),
        T9Key("？", code = KEYCODE_QUESTION),
        T9Key("！", code = KEYCODE_EXCLAMATION),
    )

    val center9Keys = listOf(
        listOf(T9Key("1", code = KEYCODE_T9_1), T9Key("ABC", "2", KEYCODE_T9_2), T9Key("DEF", "3", KEYCODE_T9_3)),
        listOf(T9Key("GHI", "4", KEYCODE_T9_4), T9Key("JKL", "5", KEYCODE_T9_5), T9Key("MNO", "6", KEYCODE_T9_6)),
        listOf(T9Key("PQRS", "7", KEYCODE_T9_7), T9Key("TUV", "8", KEYCODE_T9_8), T9Key("WXYZ", "9", KEYCODE_T9_9)),
    )

    val rightFunctions = listOf(
        T9Key("⌫", "删除", KEYCODE_DELETE),
        T9Key("重输", code = KEYCODE_CLEAR),
        T9Key("@", code = KEYCODE_AT),
    )

    val bottomToolbar = listOf(
        T9Key("符号", code = KEYCODE_SYMBOL),
        T9Key("123", code = KEYCODE_NUMBER),
        T9Key("空格", code = ' '.code),
        T9Key("中/英", code = KEYCODE_SWITCH_LANG),
        T9Key("确认", code = KEYCODE_ENTER),
    )
}

const val KEYCODE_T9_1 = '1'.code
const val KEYCODE_T9_2 = '2'.code
const val KEYCODE_T9_3 = '3'.code
const val KEYCODE_T9_4 = '4'.code
const val KEYCODE_T9_5 = '5'.code
const val KEYCODE_T9_6 = '6'.code
const val KEYCODE_T9_7 = '7'.code
const val KEYCODE_T9_8 = '8'.code
const val KEYCODE_T9_9 = '9'.code
const val KEYCODE_CLEAR = -10
const val KEYCODE_COMMA = ','.code
const val KEYCODE_PERIOD = '.'.code
const val KEYCODE_QUESTION = '?'.code
const val KEYCODE_EXCLAMATION = '!'.code
const val KEYCODE_AT = '@'.code
const val KEYCODE_ENTER = '\n'.code
const val KEYCODE_SWITCH_LANG = -11
