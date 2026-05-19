package dev.yeying.ime

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YeyingKeyboard() {
    Text(
        text = "曳影输入法",
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .wrapContentSize()
            .padding(16.dp),
    )
}
