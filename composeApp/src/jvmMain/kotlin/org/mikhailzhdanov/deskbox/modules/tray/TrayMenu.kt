package org.mikhailzhdanov.deskbox.modules.tray

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState
import com.kdroid.composetray.tray.api.Tray
import com.kdroid.composetray.utils.isMenuBarInDarkMode
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.tray_item_placeholder
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.APP_NAME
import org.mikhailzhdanov.deskbox.tools.OldWindowsChecker

@Composable
fun ApplicationScope.TrayMenu(
    showWindowHandler: () -> Unit,
    exitHandler: () -> Unit
) {
    val viewModel = remember { TrayMenuViewModel() }
    val state by viewModel.uiState.collectAsState()

    if (OldWindowsChecker.isOldWindows) {
        Tray(
            icon = painterResource(state.icon),
            state = rememberTrayState(),
            tooltip = APP_NAME,
            onAction = showWindowHandler,
            menu = {
                Item(
                    text = "Open",
                    onClick = showWindowHandler
                )

                Separator()

                state.items.forEach { menuItem ->
                    CheckboxItem(
                        text = menuItem.title,
                        checked = menuItem.checked,
                        enabled = menuItem.enabled,
                        onCheckedChange = {
                            menuItem.onClick()
                        }
                    )
                }

                Separator()

                Item(
                    text = "Exit",
                    onClick = exitHandler
                )
            }
        )
    } else {
        Tray(
            icon = painterResource(state.icon),
            tooltip = APP_NAME,
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
}