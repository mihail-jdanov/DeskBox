package org.mikhailzhdanov.deskbox.modules.tray

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import com.kdroid.composetray.tray.api.Tray
import com.kdroid.composetray.utils.isMenuBarInDarkMode
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.tray_item_placeholder
import org.jetbrains.compose.resources.painterResource

@Composable
fun ApplicationScope.TrayMenu(
    showWindowHandler: () -> Unit,
    exitHandler: () -> Unit
) {
    val viewModel = remember { TrayMenuViewModel() }
    val state by viewModel.uiState.collectAsState()

    Tray(
        icon = painterResource(state.icon),
        tooltip = "DeskBox",
        primaryAction = showWindowHandler,
        menuContent = {
            Item(
                label = "Open",
                onClick = showWindowHandler
            )

            Divider()

            state.items.forEach { menuItem ->
                Item(
                    label = menuItem.title,
                    iconContent = {
                        if (menuItem.checked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isMenuBarInDarkMode()) Color.White else Color.Black,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.tray_item_placeholder),
                                contentDescription = null
                            )
                        }
                    },
                    isEnabled = menuItem.enabled,
                    onClick = menuItem.onClick
                )
            }

            Divider()

            Item(
                label = "Exit",
                onClick = exitHandler
            )
        }
    )
}