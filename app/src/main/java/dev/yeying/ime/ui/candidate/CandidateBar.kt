package dev.yeying.ime.ui.candidate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.ui.keyboard.KeyboardAction
import dev.yeying.ime.ui.keyboard.KeyboardViewModel

@Composable
fun CandidateBar(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.composingText.isNotEmpty()) {
            Text(
                text = state.composingText,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
        }

        state.candidates.forEachIndexed { index, candidate ->
            Text(
                text = "${index + 1}. ${candidate.text}",
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    viewModel.onAction(KeyboardAction.CandidateSelect(index))
                },
            )
        }
    }
}
