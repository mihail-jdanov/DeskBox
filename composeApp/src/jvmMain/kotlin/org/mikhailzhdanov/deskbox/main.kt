package org.mikhailzhdanov.deskbox

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.tray_icon
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.managers.AutorunManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.modules.menu.MenuScreen
import org.mikhailzhdanov.deskbox.modules.tray.TrayMenu

private val windowState = WindowState(
    position = SettingsManager.loadWindowPosition(),
    width = SettingsManager.windowSize.width.dp,
    height = SettingsManager.windowSize.height.dp
)

private var composeWindow: ComposeWindow? = null

fun main() = application {
    val minimizeOnLaunch = SettingsManager.minimizeOnLaunch.value
    var windowVisible by remember { mutableStateOf(!minimizeOnLaunch) }
    val windowIcon = painterResource(Res.drawable.tray_icon)

    LaunchedEffect(Unit) {
        if (SettingsManager.autostartProfile.value) {
            val profile = ProfilesManager.profiles.value.firstOrNull {
                it.id == SettingsManager.selectedProfileID.value
            }
            profile?.let {
                SingBoxManager.start(profile)
            }
        }
        val isTaskActive = AutorunManager.isTaskActive()
        if (SettingsManager.launchWithSystem.value != isTaskActive) {
            SettingsManager.setLaunchWithSystem(isTaskActive)
        }
    }

    TrayMenu(
        showWindowHandler = {
            windowVisible = true
            composeWindow?.requestFocus()
        },
        exitHandler = {
            SingBoxManager.stop()
            SettingsManager.saveWindowPosition(windowState.position)
            exitApplication()
        }
    )

    if (windowVisible) {
        Window(
            onCloseRequest = {
                SettingsManager.saveWindowPosition(windowState.position)
                windowVisible = false
            },
            state = windowState,
            title = "DeskBox",
            icon = windowIcon,
            resizable = false
        ) {
            composeWindow = window

            MaterialTheme(
                colorScheme = colorScheme
            ) {
                MenuScreen()
            }
        }
    }
}