package org.mikhailzhdanov.deskbox.modules.editProfile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.views.customTextField.CustomTextField
import org.mikhailzhdanov.deskbox.views.TitledView
import org.mikhailzhdanov.deskbox.views.customTextField.CustomTextFieldType

@Composable
fun EditProfileScreen(
    profile: Profile,
    saveSuccessHandler: () -> Unit
) {
    val viewModel = remember { EditProfileViewModel(profile, saveSuccessHandler) }
    val state by viewModel.uiState.collectAsState()

    TitledView(
        title = if (viewModel.isEditingExistingProfile) "Edit profile" else "Create profile",
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            RowContainer {
                Text("Name")

                CustomTextField(
                    value = state.profile.name,
                    onValueChange = { value ->
                        viewModel.setName(value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    maxLength = 200,
                    placeholder = "Required",
                    leftAligned = false
                )
            }

            Separator()

            RowContainer {
                Text("Remote config")
                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = state.profile.isRemote,
                    onCheckedChange = {
                        viewModel.setIsRemote(it)
                    }
                )
            }

            Separator()

            if (state.profile.isRemote) {
                RowContainer {
                    Text("Remote config URL")

                    CustomTextField(
                        value = state.profile.remoteURL,
                        onValueChange = { value ->
                            viewModel.setRemoteURL(value)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        maxLength = 200,
                        placeholder = "Required",
                        leftAligned = false
                    )
                }

                Separator()

                RowContainer {
                    Text("Auto update")
                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = state.profile.autoUpdate,
                        onCheckedChange = {
                            viewModel.setAutoUpdate(it)
                        }
                    )
                }

                Separator()

                if (state.profile.autoUpdate) {
                    RowContainer {
                        Text("Auto update interval (minutes)")

                        CustomTextField(
                            value = state.profile.autoUpdateInterval?.toString() ?: "",
                            onValueChange = { value ->
                                viewModel.setAutoUpdateInterval(value.toLongOrNull())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            maxLength = 6,
                            placeholder = "Required",
                            leftAligned = false,
                            inputFilter = { input ->
                                val digits = input.filter { it.isDigit() }
                                val number = digits.toLongOrNull() ?: 0
                                if (number > 0) number.toString() else ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isConfigInvalid) {
                    Text(
                        text = viewModel.invalidConfigTitle,
                        color = MaterialTheme.colorScheme.error
                    )

                    FilledTonalIconButton(
                        onClick = { viewModel.showConfigErrorAlert() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionMark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (state.configButtonMode != ConfigButtonMode.None) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.showConfigDialog()
                        }
                    ) {
                        Text(
                            text = when (state.configButtonMode) {
                                ConfigButtonMode.Edit -> "Edit config"
                                ConfigButtonMode.View -> "View config"
                                else -> ""
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        viewModel.save()
                    },
                    enabled = state.isSaveAvailable
                ) {
                    Text("Save")
                }
            }
        }
    }

    if (state.showConfigDialog) {
        Dialog(
            onDismissRequest = { viewModel.hideConfigDialog() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.padding(32.dp)
            ) {
                val isEditMode = state.configButtonMode == ConfigButtonMode.Edit

                TitledView(if (isEditMode) "Edit config" else "View config") {
                    CustomTextField(
                        value = if (isEditMode) state.profile.config else profile.config,
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
                            if (isEditMode) input else profile.config
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
    }
}

@Composable
private fun RowContainer(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun Separator() {
    HorizontalDivider(modifier = Modifier.alpha(0.25f))
}