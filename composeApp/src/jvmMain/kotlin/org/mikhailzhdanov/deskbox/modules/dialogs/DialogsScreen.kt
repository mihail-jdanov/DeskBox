package org.mikhailzhdanov.deskbox.modules.dialogs

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import org.mikhailzhdanov.deskbox.DEFAULT_ANIMATION_DURATION
import org.mikhailzhdanov.deskbox.extensions.toFixedSize
import org.mikhailzhdanov.deskbox.managers.DialogData
import org.mikhailzhdanov.deskbox.modules.configOverrideValue.ConfigOverrideValueScreen
import org.mikhailzhdanov.deskbox.views.CustomAlertDialog
import org.mikhailzhdanov.deskbox.views.CustomDialog

@Composable
fun DialogsScreen() {
    val viewModel = remember { DialogsViewModel() }
    val state by viewModel.uiState.collectAsState()

    state.dialogs.toFixedSize(10).forEachIndexed { index, dialog ->
        Crossfade(
            targetState = dialog,
            animationSpec = tween(DEFAULT_ANIMATION_DURATION),
            label = "dialog$index"
        ) { dialog ->
            dialog?.let { dialog ->
                CustomDialog(
                    onDismissRequest = dialog.onDismissRequest,
                    content = dialog.content
                )
            }
        }
    }

    Crossfade(
        targetState = state.alertData,
        animationSpec = tween(DEFAULT_ANIMATION_DURATION),
        label = "alertData"
    ) { alertData ->
        alertData?.let { alertData ->
            CustomAlertDialog(
                onDismissRequest = {
                    viewModel.hideAlert()
                },
                data = alertData
            )
        }
    }

    Crossfade(
        targetState = state.isLoading,
        animationSpec = tween(DEFAULT_ANIMATION_DURATION),
        label = "isLoading"
    ) { isLoading ->
        if (isLoading) {
            CustomDialog(onDismissRequest = {}) {
                CircularProgressIndicator(
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }
    }

    Crossfade(
        targetState = state.showConfigOverrideValueDialog,
        animationSpec = tween(DEFAULT_ANIMATION_DURATION),
        label = "showConfigOverrideValueDialog"
    ) { showDialog ->
        if (showDialog) {
            val onDismiss = { viewModel.hideConfigOverrideValueDialog() }
            val data = DialogData(
                id = "",
                onDismissRequest = onDismiss,
            ) {
                ConfigOverrideValueScreen(closeHandler = onDismiss)
            }
            CustomDialog(
                onDismissRequest = onDismiss,
                content = data.content
            )
        }
    }
}