package org.mikhailzhdanov.deskbox.managers

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.GraphicsEnvironment
import java.util.prefs.Preferences

object SettingsManager {

    private const val WINDOW_POSITION_X = "windowPositionX"
    private const val WINDOW_POSITION_Y = "windowPositionY"
    private const val SELECTED_PROFILE_ID_KEY = "selectedProfileID"
    private const val AUTOSTART_PROFILE = "autostartProfile"
    private const val LAUNCH_WITH_SYSTEM = "launchWithSystem"
    private const val MINIMIZE_ON_LAUNCH_KEY = "minimizeOnLaunch"

    private val prefs = Preferences.userRoot().node("DeskBox")

    private val _selectedProfileID = MutableStateFlow(
        prefs.get(SELECTED_PROFILE_ID_KEY, "")
    )

    private val _autostartProfile = MutableStateFlow(
        prefs.getBoolean(AUTOSTART_PROFILE, false)
    )

    private val _launchWithSystem = MutableStateFlow(
        prefs.getBoolean(LAUNCH_WITH_SYSTEM, false)
    )

    private val _minimizeOnLaunch = MutableStateFlow(
        prefs.getBoolean(MINIMIZE_ON_LAUNCH_KEY, false)
    )

    val windowSize = Size(720f, 540f)
    val selectedProfileID = _selectedProfileID.asStateFlow()
    val autostartProfile = _autostartProfile.asStateFlow()
    val launchWithSystem = _launchWithSystem.asStateFlow()
    val minimizeOnLaunch = _minimizeOnLaunch.asStateFlow()

    fun setSelectedProfileID(id: String?) {
        _selectedProfileID.value = id ?: ""
        prefs.put(SELECTED_PROFILE_ID_KEY, id ?: "")
    }

    fun setAutostartProfile(value: Boolean) {
        _autostartProfile.value = value
        prefs.putBoolean(AUTOSTART_PROFILE, value)
    }

    fun setLaunchWithSystem(value: Boolean) {
        if (value) {
            AutorunManager.createTask()
            if (AutorunManager.isTaskActive()) {
                _launchWithSystem.value = true
                prefs.putBoolean(LAUNCH_WITH_SYSTEM, true)
            }
        } else {
            AutorunManager.removeTask()
            if (!AutorunManager.isTaskActive()) {
                _launchWithSystem.value = false
                prefs.putBoolean(LAUNCH_WITH_SYSTEM, false)
            }
        }
    }

    fun setMinimizeOnLaunch(value: Boolean) {
        _minimizeOnLaunch.value = value
        prefs.putBoolean(MINIMIZE_ON_LAUNCH_KEY, value)
    }

    fun saveWindowPosition(position: WindowPosition) {
        prefs.putInt(WINDOW_POSITION_X, position.x.value.toInt())
        prefs.putInt(WINDOW_POSITION_Y, position.y.value.toInt())
    }

    fun loadWindowPosition(): WindowPosition {
        val x = prefs.getInt(WINDOW_POSITION_X, Int.MIN_VALUE)
        val y = prefs.getInt(WINDOW_POSITION_Y, Int.MIN_VALUE)

        return if (x != Int.MIN_VALUE && y != Int.MIN_VALUE) {
            val screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
            val fixedX = x.coerceIn(0, screenBounds.width - windowSize.width.toInt())
            val fixedY = y.coerceIn(0, screenBounds.height - windowSize.height.toInt())
            WindowPosition(fixedX.dp, fixedY.dp)
        } else {
            WindowPosition.Aligned(Alignment.Center)
        }
    }

}