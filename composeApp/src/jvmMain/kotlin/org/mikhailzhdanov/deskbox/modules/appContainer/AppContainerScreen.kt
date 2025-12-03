package org.mikhailzhdanov.deskbox.modules.appContainer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog

val appContainerViewModel = AppContainerViewModel()

@Composable
fun AppContainerScreen(
    content: @Composable () -> Unit
) {
    val viewModel = appContainerViewModel
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        content()

        if (state.isLoading) {
            Dialog(onDismissRequest = {}) {
                CircularProgressIndicator(
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }

        if (state.alertText.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { viewModel.setAlertText(null) },
                text = {
                    Text(state.alertText)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.setAlertText(null)
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}