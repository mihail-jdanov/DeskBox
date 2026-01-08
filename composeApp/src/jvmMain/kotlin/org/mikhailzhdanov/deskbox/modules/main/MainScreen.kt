package org.mikhailzhdanov.deskbox.modules.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.lists
import deskbox.composeapp.generated.resources.theme_auto
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.Theme
import org.mikhailzhdanov.deskbox.modules.about.AboutScreen
import org.mikhailzhdanov.deskbox.modules.profiles.ProfilesScreen
import org.mikhailzhdanov.deskbox.modules.control.ControlScreen

@Composable
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
            Box {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 4.dp)
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
                        .size(80.dp)
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.switchTheme() }
                    ) {
                        when (state.preferredTheme) {
                            Theme.Auto -> Icon(
                                painter = painterResource(Res.drawable.theme_auto),
                                contentDescription = null
                            )
                            Theme.Light -> Icon(
                                imageVector = Icons.Outlined.WbSunny,
                                contentDescription = null
                            )
                            Theme.Dark -> Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 0.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                when (state.selectedTab) {
                    0 -> ControlScreen()
                    1 -> ProfilesScreen()
                    2 -> AboutScreen()
                }
            }
        }
    }
}