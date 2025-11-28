package org.mikhailzhdanov.deskbox.modules.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.lists
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mikhailzhdanov.deskbox.modules.about.AboutScreen
import org.mikhailzhdanov.deskbox.modules.profiles.ProfilesScreen
import org.mikhailzhdanov.deskbox.modules.control.ControlScreen

@Composable
@Preview
fun MenuScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavigationRail(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            NavigationRailItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                label = { Text("Control") }
            )

            NavigationRailItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.lists),
                        contentDescription = null
                    )
                },
                label = { Text("Profiles") }
            )

            NavigationRailItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                label = { Text("About") }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            when (selectedTab) {
                0 -> ControlScreen()
                1 -> ProfilesScreen()
                2 -> AboutScreen()
            }
        }
    }
}