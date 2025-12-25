package org.mikhailzhdanov.deskbox.modules.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import org.mikhailzhdanov.deskbox.views.CustomAlertDialog
import org.mikhailzhdanov.deskbox.views.CustomDialog

@Composable
fun DialogsScreen() {
    val viewModel = remember { DialogsViewModel() }
    val state by viewModel.uiState.collectAsState()

    state.dialogs.forEach { dialog ->
        Box {
            CustomDialog(
                onDismissRequest = dialog.onDismissRequest,
                content = dialog.content
            )
        }
    }

    state.alertData?.let { alertData ->
        CustomAlertDialog(
            onDismissRequest = {
                viewModel.hideAlert()
            },
            data = alertData
        )
    }

    if (state.isLoading) {
        CustomDialog(onDismissRequest = {}) {
            CircularProgressIndicator(
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }
    }
}