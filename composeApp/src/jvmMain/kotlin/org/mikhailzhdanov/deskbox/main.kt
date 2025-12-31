package org.mikhailzhdanov.deskbox

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.jthemedetecor.OsThemeDetector
import com.kdroid.composetray.utils.SingleInstanceManager
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.managers.AutorunManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.modules.dialogs.DialogsScreen
import org.mikhailzhdanov.deskbox.modules.main.MainScreen
import org.mikhailzhdanov.deskbox.modules.tray.TrayMenu
import org.mikhailzhdanov.deskbox.views.TitleBar
import java.awt.Frame
import kotlin.io.path.readText
import kotlin.io.path.writeText

const val APP_NAME = "DeskBox"

private var windowState = getWindowState()
private var composeWindow: ComposeWindow? = null

fun main(args: Array<String>) = application {
    val minimizeOnLaunch = SettingsManager.minimizeOnLaunch.value
    var windowVisible by remember { mutableStateOf(!minimizeOnLaunch) }
    val windowIcon = painterResource(Res.drawable.app_icon)
    val detector = OsThemeDetector.getDetector()
    var isSystemInDarkTheme by remember { mutableStateOf(detector.isDark) }
    val theme by SettingsManager.preferredTheme.collectAsState()

    detector.registerListener { isDark ->
        isSystemInDarkTheme = isDark
    }

    val isSingleInstance = SingleInstanceManager.isSingleInstance(
        onRestoreFileCreated = {
            args.firstOrNull()?.let(::writeText)
        },
        onRestoreRequest = {
            if (windowVisible) {
                restoreAndFocusWindow()
            } else {
                windowState = getWindowState()
                windowVisible = true
            }
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
            if (windowVisible) {
                restoreAndFocusWindow()
            } else {
                windowState = getWindowState()
                windowVisible = true
            }
        },
        exitHandler = {
            SingBoxManager.stop()
            SettingsManager.saveWindowPosition(windowState.position)
            exitApplication()
        }
    )

    val closeAction = {
        SettingsManager.saveWindowPosition(windowState.position)
        windowVisible = false
    }

    Window(
        onCloseRequest = closeAction,
        state = windowState,
        visible = windowVisible,
        title = APP_NAME,
        icon = windowIcon,
        undecorated = true,
        transparent = true,
        resizable = false
    ) {
        composeWindow = window

        LaunchedEffect(windowVisible) {
            if (windowVisible) { restoreAndFocusWindow() }
        }

        MaterialTheme(
            colorScheme = when (Theme.fromRawValue(theme)) {
                Theme.Auto -> {
                    if (isSystemInDarkTheme) darkColorScheme else lightColorScheme
                }
                Theme.Light -> lightColorScheme
                Theme.Dark -> darkColorScheme
            }
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    TitleBar(
                        title = APP_NAME,
                        icon = windowIcon,
                        closeAction = closeAction
                    )

                    Box(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomEnd = 8.dp,
                                bottomStart = 8.dp
                            )
                        )
                    ) {
                        MainScreen()
                        DialogsScreen()
                    }
                }
            }
        }
    }
}

private fun getWindowState(): WindowState {
    return WindowState(
        position = SettingsManager.loadWindowPosition(),
        width = SettingsManager.windowSize.width.dp,
        height = SettingsManager.windowSize.height.dp
    )
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