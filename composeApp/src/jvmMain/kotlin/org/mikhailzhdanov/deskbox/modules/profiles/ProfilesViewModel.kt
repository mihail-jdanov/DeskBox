package org.mikhailzhdanov.deskbox.modules.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager

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
            ProfilesManager.profiles.collect { profiles ->
                _uiState.update { it.copy(profiles = profiles) }
            }
        }
    }

    fun showEditProfileDialog(profile: Profile = Profile()) {
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

}