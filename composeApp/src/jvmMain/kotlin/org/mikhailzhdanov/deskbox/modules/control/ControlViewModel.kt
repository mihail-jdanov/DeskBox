package org.mikhailzhdanov.deskbox.modules.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager

class ControlViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        ControlUIState(
            isRunning = SingBoxManager.isRunning.value,
            version = SingBoxManager.version.value,
            isStartAvailable = !SingBoxManager.version.value.startsWith(SingBoxManager.ERROR_PREFIX)
                    && fetchSelectedProfile(SettingsManager.selectedProfileID.value) != null,
            isVersionError = SingBoxManager.version.value.startsWith(SingBoxManager.ERROR_PREFIX),
            profiles = ProfilesManager.profiles.value,
            selectedProfile = fetchSelectedProfile(SettingsManager.selectedProfileID.value),
            showProfileDropdown = false
        )
    )

    private val _logs = MutableStateFlow(SingBoxManager.logs.value)

    val uiState = _uiState.asStateFlow()
    val logs = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                combine(
                    SingBoxManager.isRunning,
                    SingBoxManager.version,
                    ProfilesManager.profiles,
                    SettingsManager.selectedProfileID
                ) { isRunning, version, profiles, selectedProfileID ->
                    ObservedValues(isRunning, version, profiles, selectedProfileID)
                }.collect { values ->
                    _uiState.update { current ->
                        current.copy(
                            isRunning = values.isRunning,
                            version = values.version,
                            isStartAvailable = !values.version.startsWith(SingBoxManager.ERROR_PREFIX)
                                    && fetchSelectedProfile(values.selectedProfileID) != null,
                            isVersionError = values.version.startsWith(SingBoxManager.ERROR_PREFIX),
                            profiles = values.profiles,
                            selectedProfile = fetchSelectedProfile(values.selectedProfileID)
                        )
                    }
                }
            }
            launch {
                SingBoxManager.logs.collect { logs ->
                    _logs.value = logs
                }
            }
        }
    }

    fun start() {
        _uiState.value.selectedProfile?.let { profile ->
            SingBoxManager.start(profile)
        }
    }

    fun stop() {
        SingBoxManager.stop()
    }

    fun setShowProfileDropdown(value: Boolean) {
        _uiState.update { it.copy(showProfileDropdown = value) }
    }

    fun selectProfile(profile: Profile) {
        if (_uiState.value.isRunning && SettingsManager.selectedProfileID.value != profile.id) {
            SingBoxManager.stop()
            SingBoxManager.start(profile)
        }
        SettingsManager.setSelectedProfileID(profile.id)
    }

    private fun fetchSelectedProfile(selectedProfileID: String): Profile? {
        val profiles = ProfilesManager.profiles.value
        return profiles.firstOrNull { it.id == selectedProfileID }
    }

}

private data class ObservedValues(
    val isRunning: Boolean,
    val version: String,
    val profiles: List<Profile>,
    val selectedProfileID: String
)