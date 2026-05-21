package io.github.cgfhsc.agileshadow.ime.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardAction
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardViewModel

private val emojiCategories = listOf(
    SymbolGridCategory("表情", listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "😉",
        "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙", "🥲",
        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
        "🫡", "🤐", "🤨", "😐", "😑", "😶", "🫥", "😏", "😒", "🙄",
        "😬", "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕",
        "🤢", "🤮", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸",
        "😎", "🤓", "🧐", "😕", "🫤", "😟", "🙁", "😮", "😯", "😲",
        "😳", "🥺", "🥹", "😦", "😧", "😨", "😰", "😥", "😢", "😭",
        "😱", "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡",
        "😠", "🤬", "😈", "👿", "💀", "☠️", "💩", "🤡", "👹", "👺",
    )),
    SymbolGridCategory("手势", listOf(
        "👍", "👎", "👌", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈",
        "👉", "👆", "🖕", "👇", "☝️", "🫵", "👋", "🤚", "🖐️", "✋",
        "🖖", "🫱", "🫲", "🫳", "🫴", "👏", "🙌", "🫶", "👐", "🤲",
        "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶",
    )),
    SymbolGridCategory("心形", listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
        "💟", "♥️", "🫀", "💌", "💋", "💍", "💎", "🔮", "🪄", "✨",
    )),
    SymbolGridCategory("自然", listOf(
        "☀️", "🌙", "⭐", "🌟", "💫", "✨", "🔥", "🌈", "☁️", "⛅",
        "⛈️", "🌤️", "🌥️", "🌦️", "🌧️", "🌨️", "🌩️", "🌪️", "🌫️", "🌬️",
        "🌸", "💮", "🌹", "🥀", "🌺", "🌻", "🌼", "🌷", "🌱", "🪴",
        "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂",
        "🍃", "🪹", "🪺", "🍄", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉",
    )),
    SymbolGridCategory("物品", listOf(
        "🎉", "🎊", "🎈", "🎁", "🎀", "🏆", "🥇", "🥈", "🥉", "🏅",
        "🎵", "🎶", "🎸", "🎹", "🎺", "🎻", "🥁", "📱", "💻", "🖥️",
        "📷", "📹", "🎬", "🎮", "🕹️", "🎲", "♟️", "🎯", "🏀", "⚽",
        "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓",
        "🏠", "🏢", "🏣", "🏤", "🏥", "🏦", "🏨", "🏩", "🏪", "🏫",
    )),
    SymbolGridCategory("交通", listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
        "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴", "🛹", "🛼",
        "✈️", "🛩️", "🛫", "🛬", "🪂", "💺", "🚁", "🚟", "🚠", "🚡",
        "🚀", "🛸", "🚢", "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚡", "🗺️",
    )),
)

@Composable
fun EmojiPanel(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    CategorizedGridPanel(
        categories = emojiCategories,
        onItemClick = { viewModel.onAction(KeyboardAction.DirectCommit(it)) },
        modifier = modifier,
        isDark = isDark,
    )
}
