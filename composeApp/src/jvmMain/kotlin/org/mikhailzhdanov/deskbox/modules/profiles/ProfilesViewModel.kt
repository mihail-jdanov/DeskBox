package org.mikhailzhdanov.deskbox.modules.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.AlertsManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.tools.RemoteConfigsFetcher
import org.mikhailzhdanov.deskbox.tools.TimestampFormatter

class ProfilesViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfilesUIState(
            profiles = ProfilesManager.profiles.value,
            profileForEditing = null,
            profileForDeletion = null
        )
    )

    val uiState = _uiState.asStateFlow()
    val profilesLimit = 100

    init {
        viewModelScope.launch {
            launch {
                ProfilesManager.profiles.collect { profiles ->
                    _uiState.update { it.copy(profiles = profiles) }
                }
            }
            launch {
                ProfilesManager.profileToImport.collect { profileToImport ->
                    profileToImport?.let { profile ->
                        showEditProfileDialog(profile)
                    }
                }
            }
        }
    }

    fun showEditProfileDialog(profile: Profile = Profile()) {
        if (SingBoxManager.version.value.startsWith(SingBoxManager.ERROR_PREFIX)) {
            AlertsManager.setAlert(SingBoxManager.version.value)
            return
        }
        _uiState.update { it.copy(profileForEditing = profile) }
    }

    fun hideEditProfileDialog() {
        _uiState.update { it.copy(profileForEditing = null) }
    }

    fun showDeleteProfileDialog(profile: Profile) {
        _uiState.update { it.copy(profileForDeletion = profile) }
    }

    fun hideDeleteProfileDialog() {
        _uiState.update { it.copy(profileForDeletion = null) }
    }

    fun deleteProfile(profile: Profile) {
        ProfilesManager.deleteProfile(profile)
        if (SingBoxManager.lastStartedProfile?.id == profile.id) {
            SingBoxManager.stop()
        }
    }

    fun updateProfileConfig(profile: Profile) {
        if (!profile.isRemote) return
        AlertsManager.setLoading(true)
        viewModelScope.launch {
            try {
                val config = RemoteConfigsFetcher.fetchConfig(profile.remoteURL)
                AlertsManager.setLoading(false)
                val configError = SingBoxManager.validateConfig(config)
                if (configError.isEmpty()) {
                    val updatedProfile = profile.copy(
                        config = config,
                        lastUpdateTimestamp = TimestampFormatter.getCurrentTimestamp()
                    )
                    ProfilesManager.saveProfile(updatedProfile)
                } else {
                    AlertsManager.setAlert(
                        "Invalid config\n\n$configError"
                    )
                }
            } catch (e: Exception) {
                AlertsManager.setLoading(false)
                AlertsManager.setAlert(e.message ?: "")
            }
        }
    }

}