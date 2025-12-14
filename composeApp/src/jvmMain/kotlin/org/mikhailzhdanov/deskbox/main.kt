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
import com.kdroid.composetray.utils.SingleInstanceManager
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.tray_icon
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.managers.AutorunManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.modules.main.MainScreen
import org.mikhailzhdanov.deskbox.modules.tray.TrayMenu
import java.awt.Frame
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val windowState = WindowState(
    position = SettingsManager.loadWindowPosition(),
    width = SettingsManager.windowSize.width.dp,
    height = SettingsManager.windowSize.height.dp
)

private var composeWindow: ComposeWindow? = null

fun main(args: Array<String>) = application {
    val minimizeOnLaunch = SettingsManager.minimizeOnLaunch.value
    var windowVisible by remember { mutableStateOf(!minimizeOnLaunch) }
    val windowIcon = painterResource(Res.drawable.tray_icon)

    val isSingleInstance = SingleInstanceManager.isSingleInstance(
        onRestoreFileCreated = {
            args.firstOrNull()?.let(::writeText)
        },
        onRestoreRequest = {
            windowVisible = true
            restoreAndFocusWindow()
            ProfilesManager.importRemoteProfile(readText())
        }
    )

    if (!isSingleInstance) {
        exitApplication()
        return@application
    }

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
        registerSingBoxLinks()
    }

    TrayMenu(
        showWindowHandler = {
            windowVisible = true
            restoreAndFocusWindow()
        },
        exitHandler = {
            SingBoxManager.stop()
            SettingsManager.saveWindowPosition(windowState.position)
            exitApplication()
        }
    )

    Window(
        onCloseRequest = {
            SettingsManager.saveWindowPosition(windowState.position)
            windowVisible = false
        },
        state = windowState,
        visible = windowVisible,
        title = "DeskBox",
        icon = windowIcon,
        resizable = false
    ) {
        composeWindow = window

        MaterialTheme(
            colorScheme = colorScheme
        ) {
            MainScreen()
        }
    }
}

private fun restoreAndFocusWindow() {
    composeWindow?.apply {
        if (extendedState == Frame.ICONIFIED) {
            extendedState = Frame.NORMAL
        }
        toFront()
        requestFocus()
    }
}

private fun registerSingBoxLinks() {
    ProcessBuilder(
        "reg", "add", "HKEY_CURRENT_USER\\Software\\Classes\\sing-box",
        "/f", "/v", "URL Protocol", "/d", ""
    ).start().waitFor()
    ProcessBuilder(
        "reg", "add",
        "HKEY_CURRENT_USER\\Software\\Classes\\sing-box\\shell\\open\\command",
        "/f", "/ve", "/d", "${AutorunManager.pathToExecutable} \"\"%1\"\""
    ).start().waitFor()
}