package org.mikhailzhdanov.deskbox.modules.editConfig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mikhailzhdanov.deskbox.modules.editProfile.ConfigButtonMode
import org.mikhailzhdanov.deskbox.modules.editProfile.EditProfileViewModel
import org.mikhailzhdanov.deskbox.views.CustomTextField
import org.mikhailzhdanov.deskbox.views.TitledView
import org.mikhailzhdanov.deskbox.views.CustomTextFieldType

const val EDIT_CONFIG_SCREEN_ID = "EditConfigScreen"

@Composable
fun EditConfigScreen(
    viewModel: EditProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.padding(32.dp)
    ) {
        val isEditMode = state.configButtonMode == ConfigButtonMode.Edit

        TitledView(if (isEditMode) "Edit config" else "View config") {
            CustomTextField(
                value = if (isEditMode) state.profile.config else viewModel.profile.config,
                onValueChange = { value ->
                    if (isEditMode) {
                        viewModel.setConfig(value)
                    }
                },
                modifier = Modifier.padding(top = 8.dp).fillMaxSize(),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                type = CustomTextFieldType.MultilineWithScrollbar,
                inputFilter = { input ->
                    if (isEditMode) input else viewModel.profile.config
                }
            )
        }

        IconButton(
            onClick = { viewModel.hideConfigDialog() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        }
    }
}