package org.mikhailzhdanov.deskbox.modules.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.lists
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mikhailzhdanov.deskbox.modules.about.AboutScreen
import org.mikhailzhdanov.deskbox.modules.profiles.ProfilesScreen
import org.mikhailzhdanov.deskbox.modules.control.ControlScreen

@Composable
@Preview
fun MainScreen() {
    val viewModel = remember { MainViewModel() }
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                NavigationRailItem(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                    label = { Text("Control") }
                )

                NavigationRailItem(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.lists),
                            contentDescription = null
                        )
                    },
                    label = { Text("Profiles") }
                )

                NavigationRailItem(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                when (state.selectedTab) {
                    0 -> ControlScreen()
                    1 -> ProfilesScreen()
                    2 -> AboutScreen()
                }
            }
        }

        if (state.isLoading) {
            Dialog(onDismissRequest = {}) {
                CircularProgressIndicator(
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }

        state.alertData?.let { alertData ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.hideAlert()
                    alertData.cancelButtonData?.handler()
                },
                text = {
                    Text(alertData.text)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.hideAlert()
                            alertData.confirmButtonData.handler()
                        }
                    ) {
                        Text(alertData.confirmButtonData.title)
                    }
                },
                dismissButton = if (alertData.cancelButtonData == null) null else {{
                    TextButton(
                        onClick = {
                            viewModel.hideAlert()
                            alertData.cancelButtonData.handler()
                        }
                    ) {
                        Text(alertData.cancelButtonData.title)
                    }
                }}
            )
        }
    }
}