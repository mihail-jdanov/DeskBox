package org.mikhailzhdanov.deskbox.modules.profiles

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.modules.editProfile.EditProfileScreen
import org.mikhailzhdanov.deskbox.tools.TimestampFormatter
import org.mikhailzhdanov.deskbox.views.TitledScrollView

private val scrollState = ScrollState(0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen() {
    val viewModel = remember { ProfilesViewModel() }
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        TitledScrollView(
            title = "Profiles",
            scrollState = scrollState
        ) {
            Column {
                state.profiles.forEach { profile ->
                    Row(
                        modifier = Modifier.height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                        ) {
                            val showDate = profile.isRemote && profile.lastUpdateTimestamp > 0

                            Text(
                                text = profile.name,
                                modifier = Modifier.offset(y = (if (showDate) 3 else 0).dp),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )

                            if (showDate) {
                                val date = TimestampFormatter.format(
                                    profile.lastUpdateTimestamp
                                )

                                Text(
                                    text = "Last config update: $date",
                                    modifier = Modifier
                                        .alpha(0.5f)
                                        .offset(y = (-3).dp),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (profile.isRemote) {
                            IconButton(
                                onClick = { viewModel.updateProfileConfig(profile) },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            }
                        }

                        Box {
                            var expanded by remember { mutableStateOf(false) }

                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text("Edit")
                                    },
                                    onClick = {
                                        expanded = false
                                        viewModel.showEditProfileDialog(profile)
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Delete")
                                    },
                                    onClick = {
                                        expanded = false
                                        viewModel.showDeleteProfileDialog(profile)
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.alpha(0.25f))
                }

                Spacer(
                    modifier = Modifier.height(
                        if (state.profiles.count() < viewModel.profilesLimit) 72.dp else 0.dp
                    )
                )
            }
        }

        if (state.profiles.isEmpty()) {
            Text(
                text = "No profiles",
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(0.5f)
            )
        }

        if (state.profiles.count() < viewModel.profilesLimit) {
            FloatingActionButton(
                onClick = { viewModel.showEditProfileDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    }

    state.profileForEditing?.let { profile ->
        Dialog(
            onDismissRequest = { viewModel.hideEditProfileDialog() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                EditProfileScreen(
                    profile = profile,
                    saveSuccessHandler = {
                        viewModel.hideEditProfileDialog()
                    }
                )

                IconButton(
                    onClick = { viewModel.hideEditProfileDialog() },
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

    state.profileForDeletion?.let { profile ->
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteProfileDialog() },
            text = {
                Text("Delete profile \"${profile.name}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideDeleteProfileDialog()
                        viewModel.deleteProfile(profile)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteProfileDialog() }
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}