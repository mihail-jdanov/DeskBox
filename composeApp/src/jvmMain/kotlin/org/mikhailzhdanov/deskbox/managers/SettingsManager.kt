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

    private const val WINDOW_POSITION_X_KEY = "windowPositionX"
    private const val WINDOW_POSITION_Y_KEY = "windowPositionY"
    private const val SELECTED_PROFILE_ID_KEY = "selectedProfileID"
    private const val AUTOSTART_PROFILE_KEY = "autostartProfile"
    private const val LAUNCH_WITH_SYSTEM_KEY = "launchWithSystem"
    private const val MINIMIZE_ON_LAUNCH_KEY = "minimizeOnLaunch"
    private const val PREFERRED_THEME_KEY = "preferredTheme"
    private const val CONFIG_OVERRIDE_VALUE_KEY = "configOverrideValue"

    private val prefs = Preferences.userRoot().node("DeskBox")

    private val _selectedProfileID = MutableStateFlow(
        prefs.get(SELECTED_PROFILE_ID_KEY, "")
    )

    private val _autostartProfile = MutableStateFlow(
        prefs.getBoolean(AUTOSTART_PROFILE_KEY, false)
    )

    private val _launchWithSystem = MutableStateFlow(
        prefs.getBoolean(LAUNCH_WITH_SYSTEM_KEY, false)
    )

    private val _minimizeOnLaunch = MutableStateFlow(
        prefs.getBoolean(MINIMIZE_ON_LAUNCH_KEY, false)
    )

    private val _preferredTheme = MutableStateFlow(
        prefs.getInt(PREFERRED_THEME_KEY, 0)
    )

    private val _configOverrideValue = MutableStateFlow(
        prefs.get(CONFIG_OVERRIDE_VALUE_KEY, "")
    )

    val windowSize = Size(720f, 540f)
    val selectedProfileID = _selectedProfileID.asStateFlow()
    val autostartProfile = _autostartProfile.asStateFlow()
    val launchWithSystem = _launchWithSystem.asStateFlow()
    val minimizeOnLaunch = _minimizeOnLaunch.asStateFlow()
    val preferredTheme = _preferredTheme.asStateFlow()
    val configOverrideValue = _configOverrideValue.asStateFlow()

    fun setSelectedProfileID(id: String?) {
        _selectedProfileID.value = id ?: ""
        prefs.put(SELECTED_PROFILE_ID_KEY, id ?: "")
    }

    fun setAutostartProfile(value: Boolean) {
        _autostartProfile.value = value
        prefs.putBoolean(AUTOSTART_PROFILE_KEY, value)
    }

    fun setLaunchWithSystem(value: Boolean) {
        if (value) {
            AutorunManager.createTask { success ->
                if (success) {
                    _launchWithSystem.value = true
                    prefs.putBoolean(LAUNCH_WITH_SYSTEM_KEY, true)
                }
            }
        } else {
            AutorunManager.removeTask { success ->
                if (success) {
                    _launchWithSystem.value = false
                    prefs.putBoolean(LAUNCH_WITH_SYSTEM_KEY, false)
                }
            }
        }
    }

    fun setMinimizeOnLaunch(value: Boolean) {
        _minimizeOnLaunch.value = value
        prefs.putBoolean(MINIMIZE_ON_LAUNCH_KEY, value)
    }

    fun setPreferredTheme(value: Int) {
        _preferredTheme.value = value
        prefs.putInt(PREFERRED_THEME_KEY, value)
    }

    fun setConfigOverrideValue(value: String) {
        _configOverrideValue.value = value
        prefs.put(CONFIG_OVERRIDE_VALUE_KEY, value)
    }

    fun saveWindowPosition(position: WindowPosition) {
        prefs.putInt(WINDOW_POSITION_X_KEY, position.x.value.toInt())
        prefs.putInt(WINDOW_POSITION_Y_KEY, position.y.value.toInt())
    }

    fun loadWindowPosition(): WindowPosition {
        val x = prefs.getInt(WINDOW_POSITION_X_KEY, Int.MIN_VALUE)
        val y = prefs.getInt(WINDOW_POSITION_Y_KEY, Int.MIN_VALUE)

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