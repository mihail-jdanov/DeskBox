package org.mikhailzhdanov.deskbox.modules.tray

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.app_icon_on_raster
import deskbox.composeapp.generated.resources.app_icon_on_vector
import deskbox.composeapp.generated.resources.app_icon_on_vector_linux
import deskbox.composeapp.generated.resources.app_icon_on_vector_mac
import deskbox.composeapp.generated.resources.app_icon_raster
import deskbox.composeapp.generated.resources.app_icon_vector
import deskbox.composeapp.generated.resources.app_icon_vector_linux
import deskbox.composeapp.generated.resources.app_icon_vector_mac
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.tools.OSChecker
import org.mikhailzhdanov.deskbox.tools.OSType

class TrayMenuViewModel: ViewModel() {

    private val osType = OSChecker.currentOS.type

    private var iconDefault = when (osType) {
        OSType.Windows -> Res.drawable.app_icon_raster
        OSType.MacOS -> Res.drawable.app_icon_vector_mac
        OSType.Linux -> Res.drawable.app_icon_vector_linux
    }

    private var iconOn = when (osType) {
        OSType.Windows -> Res.drawable.app_icon_on_raster
        OSType.MacOS -> Res.drawable.app_icon_on_vector_mac
        OSType.Linux -> Res.drawable.app_icon_on_vector_linux
    }

    private val _uiState = MutableStateFlow(
        TrayMenuUIState(
            icon = if (SingBoxManager.isRunning.value) iconOn else iconDefault,
            items = createItems(
                ObservedValues(
                    isRunning = SingBoxManager.isRunning.value,
                    version = SingBoxManager.version.value,
                    selectedProfileID = SettingsManager.selectedProfileID.value,
                    autostartProfile = SettingsManager.autostartProfile.value,
                    launchWithSystem = SettingsManager.launchWithSystem.value,
                    minimizeOnLaunch = SettingsManager.minimizeOnLaunch.value
                )
            )
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                SingBoxManager.isRunning,
                SingBoxManager.version,
                SettingsManager.selectedProfileID,
                SettingsManager.autostartProfile,
                SettingsManager.launchWithSystem,
                SettingsManager.minimizeOnLaunch
            ) { values ->
                ObservedValues(
                    isRunning = values[0] as Boolean,
                    version = values[1] as String,
                    selectedProfileID = values[2] as String,
                    autostartProfile = values[3] as Boolean,
                    launchWithSystem = values[4] as Boolean,
                    minimizeOnLaunch = values[5] as Boolean
                )
            }.collect { values ->
                _uiState.update { current ->
                    current.copy(
                        icon = if (values.isRunning) iconOn else iconDefault,
                        items = createItems(values)
                    )
                }
            }
        }
    }

    fun setVectorIcons() {
        when (osType) {
            OSType.Windows -> {
                iconDefault = Res.drawable.app_icon_vector
                iconOn = Res.drawable.app_icon_on_vector
                _uiState.update {
                    it.copy(icon = if (SingBoxManager.isRunning.value) iconOn else iconDefault)
                }
            }
            OSType.MacOS, OSType.Linux -> {}
        }
    }

    private fun createItems(values: ObservedValues): List<TrayMenuItem> {
        val startItem = TrayMenuItem(
            title = "Start",
            checked = false,
            enabled = !values.version.startsWith(SingBoxManager.ERROR_PREFIX)
                    && fetchSelectedProfile(values.selectedProfileID) != null,
            onClick = {
                fetchSelectedProfile(values.selectedProfileID)?.let { profile ->
                    SingBoxManager.start(profile)
                }
            }
        )
        val stopItem = TrayMenuItem(
            title = "Stop",
            checked = false,
            enabled = true,
            onClick = {
                SingBoxManager.stop()
            }
        )
        var suffix = if (values.autostartProfile) " " else ""
        val autostartProfileItem = TrayMenuItem(
            title = "Start selected profile on launch$suffix",
            checked = values.autostartProfile,
            enabled = true,
            onClick = {
                SettingsManager.setAutostartProfile(!values.autostartProfile)
            }
        )
        suffix = if (values.launchWithSystem) " " else ""
        val launchWithSystemItem = TrayMenuItem(
            title = "Launch with system$suffix",
            checked = values.launchWithSystem,
            enabled = true,
            onClick = {
                SettingsManager.setLaunchWithSystem(!values.launchWithSystem)
            }
        )
        suffix = if (values.minimizeOnLaunch) " " else ""
        val minimizeOnLaunchItem = TrayMenuItem(
            title = "Minimize on launch$suffix",
            checked = values.minimizeOnLaunch,
            enabled = true,
            onClick = {
                SettingsManager.setMinimizeOnLaunch(!values.minimizeOnLaunch)
            }
        )
        val controlItem = if (values.isRunning) stopItem else startItem
        return listOf(controlItem, autostartProfileItem, launchWithSystemItem, minimizeOnLaunchItem)
    }

    private fun fetchSelectedProfile(selectedProfileID: String): Profile? {
        val profiles = ProfilesManager.profiles.value
        return profiles.firstOrNull { it.id == selectedProfileID }
    }

}

private data class ObservedValues(
    val isRunning: Boolean,
    val version: String,
    val selectedProfileID: String,
    val autostartProfile: Boolean,
    val launchWithSystem: Boolean,
    val minimizeOnLaunch: Boolean
)