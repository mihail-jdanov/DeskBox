package org.mikhailzhdanov.deskbox.modules.configOverrideValue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.views.CustomTextField

@Composable
fun ConfigOverrideValueScreen(
    closeHandler: () -> Unit
) {
    val viewModel = remember { ConfigOverrideValueViewModel(closeHandler) }
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier
            .width(450.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Override template value in configs (${SingBoxManager.CONFIG_OVERRIDE_VALUE_KEY})")

            CustomTextField(
                value = state.overrideText,
                onValueChange = { value ->
                    viewModel.setOverrideText(value)
                },
                modifier = Modifier.fillMaxWidth(),
                maxLength = 200,
                placeholder = "Enter value"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = closeHandler
                ) {
                    Text("Cancel")
                }

                TextButton(
                    onClick = {
                        viewModel.saveOverrideText()
                        closeHandler()
                    }
                ) {
                    Text("Apply")
                }
            }
        }
    }
}