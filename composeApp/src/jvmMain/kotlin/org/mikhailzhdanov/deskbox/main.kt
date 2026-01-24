package org.mikhailzhdanov.deskbox

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.jthemedetecor.OsThemeDetector
import com.kdroid.composetray.utils.SingleInstanceManager
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.app_icon_raster
import deskbox.composeapp.generated.resources.app_icon_vector
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.managers.AutorunManager
import org.mikhailzhdanov.deskbox.managers.DialogsManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.modules.dialogs.DialogsScreen
import org.mikhailzhdanov.deskbox.modules.main.MainScreen
import org.mikhailzhdanov.deskbox.modules.tray.TrayMenu
import org.mikhailzhdanov.deskbox.tools.OSChecker
import org.mikhailzhdanov.deskbox.tools.OSType
import org.mikhailzhdanov.deskbox.views.CustomTitleBar
import java.awt.Desktop
import java.awt.Frame
import kotlin.io.path.readText
import kotlin.io.path.writeText

const val APP_NAME = "DeskBox"
const val DEFAULT_ANIMATION_DURATION = 200

private val focusRequester = FocusRequester()

private var windowState = getWindowState()
private var composeWindow: ComposeWindow? = null
private var initialSetupCompleted = false

fun main(args: Array<String>) = application {
    val minimizeOnLaunch = SettingsManager.minimizeOnLaunch.value
    var windowVisible by remember { mutableStateOf(!minimizeOnLaunch) }
    var windowIcon by remember { mutableStateOf(Res.drawable.app_icon_raster) }
    val detector = OsThemeDetector.getDetector()
    var isSystemInDarkTheme by remember { mutableStateOf(detector.isDark) }
    val theme by SettingsManager.preferredTheme.collectAsState()
    val osType = OSChecker.currentOS.type

    if (!initialSetupCompleted) {
        val onRestoreRequest: (String) -> Unit = { arg ->
            if (windowVisible) {
                restoreAndFocusWindow()
            } else {
                windowState = getWindowState()
                windowVisible = true
            }
            ProfilesManager.importRemoteProfile(arg)
        }

        val isSingleInstance: Boolean
        when (osType) {
            OSType.Windows, OSType.Linux -> {
                isSingleInstance = SingleInstanceManager.isSingleInstance(
                    onRestoreFileCreated = {
                        args.firstOrNull()?.let(::writeText)
                    },
                    onRestoreRequest = { onRestoreRequest(readText()) }
                )
                registerSingBoxLinks()
            }
            OSType.MacOS -> {
                isSingleInstance = SingleInstanceManager.isSingleInstance {
                    if (windowVisible) restoreAndFocusWindow()
                }
                Desktop.getDesktop().setOpenURIHandler { event ->
                    onRestoreRequest(event.uri.toString())
                }
            }
        }

        if (!isSingleInstance) {
            exitApplication()
            return@application
        }

        if (!args.isEmpty()) {
            onRestoreRequest(args.first())
        }

        detector.registerListener { isDark ->
            isSystemInDarkTheme = isDark
        }

        if (SettingsManager.autostartProfile.value) {
            val profile = ProfilesManager.profiles.value.firstOrNull {
                it.id == SettingsManager.selectedProfileID.value
            }
            profile?.let {
                SingBoxManager.start(profile)
            }
        }

        AutorunManager.isTaskActive { isActive ->
            if (SettingsManager.launchWithSystem.value != isActive) {
                SettingsManager.setLaunchWithSystem(isActive)
            }
        }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                SingBoxManager.stop()
                SettingsManager.saveWindowPosition(windowState.position)
            }
        )

        initialSetupCompleted = true
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
        icon = when (osType) {
            OSType.Windows, OSType.Linux -> painterResource(windowIcon)
            OSType.MacOS -> null
        },
        undecorated = osType.needsCustomTitleBar(),
        transparent = osType.needsCustomTitleBar(),
        resizable = false,
        onKeyEvent = { event ->
            if (event.type == KeyEventType.KeyDown &&
                event.isCtrlPressed &&
                event.isShiftPressed &&
                event.isAltPressed &&
                event.key == Key.O
            ) {
                DialogsManager.setConfigOverrideValueDialog(true)
                true
            } else false
        }
    ) {
        composeWindow = window

        if (osType == OSType.MacOS) {
            window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            val lightAppearance = "NSAppearanceNameAqua"
            val darkAppearance = "NSAppearanceNameDarkAqua"
            val appearance = when (Theme.fromRawValue(theme)) {
                Theme.Auto -> {
                    if (isSystemInDarkTheme) darkAppearance else lightAppearance
                }
                Theme.Light -> lightAppearance
                Theme.Dark -> darkAppearance
            }
            window.rootPane.putClientProperty("apple.awt.windowAppearance", appearance)
        }

        LaunchedEffect(windowVisible) {
            if (windowVisible) {
                restoreAndFocusWindow()
                windowIcon = Res.drawable.app_icon_vector
            }
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
                shape = RoundedCornerShape(osType.getWindowCornerRadius().dp)
            ) {
                Column {
                    when (osType) {
                        OSType.Windows, OSType.Linux -> {
                            CustomTitleBar(
                                isWindows = osType == OSType.Windows,
                                title = APP_NAME,
                                icon = painterResource(windowIcon),
                                closeAction = closeAction
                            )
                        }
                        OSType.MacOS -> {
                            Box(modifier = Modifier.height(28.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .focusable()
                            .border(
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
    if (OSChecker.currentOS.type == OSType.MacOS) {
        Desktop.getDesktop().requestForeground(true)
    }
    focusRequester.requestFocus()
}

private fun registerSingBoxLinks() {
    when (OSChecker.currentOS.type) {
        OSType.Windows -> {
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
        OSType.MacOS -> {}
        OSType.Linux -> {
            ProcessBuilder(
                "xdg-mime",
                "default",
                "$APP_NAME.desktop",
                "x-scheme-handler/sing-box"
            ).start().waitFor()
        }
    }
}