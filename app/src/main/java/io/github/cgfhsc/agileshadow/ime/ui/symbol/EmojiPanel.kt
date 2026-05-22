package io.github.cgfhsc.agileshadow.ime.ui.symbol

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    ), icon = Icons.Outlined.EmojiEmotions),
    SymbolGridCategory("手势", listOf(
        "👍", "👎", "👌", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈",
        "👉", "👆", "🖕", "👇", "☝️", "🫵", "👋", "🤚", "🖐️", "✋",
        "🖖", "🫱", "🫲", "🫳", "🫴", "👏", "🙌", "🫶", "👐", "🤲",
        "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶",
    ), icon = Icons.Outlined.PanTool),
    SymbolGridCategory("心形", listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
        "💟", "♥️", "🫀", "💌", "💋", "💍", "💎", "🔮", "🪄", "✨",
    ), icon = Icons.Outlined.Favorite),
    SymbolGridCategory("自然", listOf(
        "☀️", "🌙", "⭐", "🌟", "💫", "✨", "🔥", "🌈", "☁️", "⛅",
        "⛈️", "🌤️", "🌥️", "🌦️", "🌧️", "🌨️", "🌩️", "🌪️", "🌫️", "🌬️",
        "🌸", "💮", "🌹", "🥀", "🌺", "🌻", "🌼", "🌷", "🌱", "🪴",
        "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁", "🍂",
        "🍃", "🪹", "🪺", "🍄", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉",
    ), icon = Icons.Outlined.Nature),
    SymbolGridCategory("物品", listOf(
        "🎉", "🎊", "🎈", "🎁", "🎀", "🏆", "🥇", "🥈", "🥉", "🏅",
        "🎵", "🎶", "🎸", "🎹", "🎺", "🎻", "🥁", "📱", "💻", "🖥️",
        "📷", "📹", "🎬", "🎮", "🕹️", "🎲", "♟️", "🎯", "🏀", "⚽",
        "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓",
        "🏠", "🏢", "🏣", "🏤", "🏥", "🏦", "🏨", "🏩", "🏪", "🏫",
    ), icon = Icons.Outlined.Celebration),
    SymbolGridCategory("交通", listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
        "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴", "🛹", "🛼",
        "✈️", "🛩️", "🛫", "🛬", "🪂", "💺", "🚁", "🚟", "🚠", "🚡",
        "🚀", "🛸", "🚢", "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚡", "🗺️",
    ), icon = Icons.Outlined.DirectionsCar),
)

@Composable
fun EmojiPanel(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    var isLocked by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    CategorizedGridPanel(
        categories = emojiCategories,
        onItemClick = { emoji ->
            viewModel.onAction(KeyboardAction.DirectCommit(emoji))
            if (!isLocked) {
                viewModel.onAction(KeyboardAction.SwitchKeyboard(state.previousKeyboard))
            }
        },
        modifier = modifier,
        isDark = isDark,
        columns = 8,
        onBack = { viewModel.onAction(KeyboardAction.SwitchKeyboard(state.previousKeyboard)) },
        isLocked = isLocked,
        onLockToggle = { isLocked = !isLocked },
        plainItem = true,
    )
}
