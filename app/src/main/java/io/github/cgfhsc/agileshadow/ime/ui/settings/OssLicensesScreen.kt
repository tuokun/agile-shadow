package io.github.cgfhsc.agileshadow.ime.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OssLicensesScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        TopAppBar(
            title = { Text("开源声明") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                    )
                }
            },
        )

        Column(modifier = Modifier.padding(16.dp)) {
            licenses.forEach { lic ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = lic.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = lic.license,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private data class OssLicense(val name: String, val license: String)

private val licenses = listOf(
    OssLicense(
        "AndroidX & Jetpack Compose",
        "Apache License 2.0\nCopyright (C) Google LLC",
    ),
    OssLicense(
        "Material Design 3",
        "Apache License 2.0\nCopyright (C) Google LLC",
    ),
    OssLicense(
        "Kotlin",
        "Apache License 2.0\nCopyright (C) JetBrains s.r.o.",
    ),
    OssLicense(
        "Room Database",
        "Apache License 2.0\nCopyright (C) Google LLC",
    ),
    OssLicense(
        "DataStore Preferences",
        "Apache License 2.0\nCopyright (C) Google LLC",
    ),
    OssLicense(
        "ML Kit Digital Ink Recognition",
        "ML Kit Terms of Service\nCopyright (C) Google LLC",
    ),
    OssLicense(
        "Emoji Compat",
        "Apache License 2.0\nCopyright (C) Google LLC",
    ),
    OssLicense(
        "Kotlin Coroutines",
        "Apache License 2.0\nCopyright (C) JetBrains s.r.o.",
    ),
    OssLicense(
        "libyuyan (核心输入法引擎)",
        "Apache License 2.0\nCopyright (C) yuyan",
    ),
)

@Composable
private fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
    )
}
