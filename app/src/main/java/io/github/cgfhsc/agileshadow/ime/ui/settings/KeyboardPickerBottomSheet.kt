package io.github.cgfhsc.agileshadow.ime.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.unit.dp
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardPickerBottomSheet(
    currentKeyboard: KeyboardType,
    onSelect: (KeyboardType) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = "默认键盘",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )

            val keyboardOptions = listOf(
                KeyboardType.QWERTY to "拼音26键",
                KeyboardType.T9 to "拼音9键",
            )

            keyboardOptions.forEachIndexed { index, (type, label) ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(type); onDismiss() }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (type == currentKeyboard) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
