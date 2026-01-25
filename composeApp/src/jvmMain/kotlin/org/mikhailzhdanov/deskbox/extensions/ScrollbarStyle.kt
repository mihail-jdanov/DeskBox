package org.mikhailzhdanov.deskbox.extensions

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ScrollbarStyle.withThemedColors(): ScrollbarStyle {
    return this.copy(
        unhoverColor = MaterialTheme.colorScheme.onBackground.copy(0.12f),
        hoverColor = MaterialTheme.colorScheme.onBackground.copy(0.5f)
    )
}