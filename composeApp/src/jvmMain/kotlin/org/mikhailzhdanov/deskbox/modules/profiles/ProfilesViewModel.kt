package org.mikhailzhdanov.deskbox.modules.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.AlertButtonData
import org.mikhailzhdanov.deskbox.managers.AlertData
import org.mikhailzhdanov.deskbox.managers.DialogsManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.modules.editProfile.EDIT_PROFILE_SCREEN_ID
import org.mikhailzhdanov.deskbox.modules.editProfile.EditProfileScreen
import org.mikhailzhdanov.deskbox.modules.profileQR.PROFILE_QR_SCREEN_ID
import org.mikhailzhdanov.deskbox.modules.profileQR.ProfileQRScreen
import org.mikhailzhdanov.deskbox.tools.RemoteConfigsFetcher
import org.mikhailzhdanov.deskbox.tools.TimestampFormatter

class ProfilesViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfilesUIState(
            profiles = ProfilesManager.profiles.value
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
            DialogsManager.setAlert(SingBoxManager.version.value)
            return
        }
        val onDismiss = { DialogsManager.removeDialog(EDIT_PROFILE_SCREEN_ID) }
        DialogsManager.addDialog(
            id = EDIT_PROFILE_SCREEN_ID,
            onDismissRequest = onDismiss,
            content = {
                EditProfileScreen(
                    profile = profile,
                    closeHandler = onDismiss
                )
            }
        )
    }

    fun showDeleteProfileDialog(profile: Profile) {
        DialogsManager.setAlert(
            AlertData(
                text = "Delete profile \"${profile.name}\"?",
                confirmButtonData = AlertButtonData(
                    title = "Delete",
                    handler = { deleteProfile(profile) }
                ),
                cancelButtonData = AlertButtonData.cancel()
            )
        )
    }

    fun updateProfileConfig(profile: Profile) {
        if (!profile.isRemote) return
        DialogsManager.setLoading(true)
        viewModelScope.launch {
            try {
                val config = RemoteConfigsFetcher.fetchConfig(profile.remoteURL)
                DialogsManager.setLoading(false)
                val configError = SingBoxManager.validateConfig(config)
                if (configError.isEmpty()) {
                    val updatedProfile = profile.copy(
                        config = config,
                        lastUpdateTimestamp = TimestampFormatter.getCurrentTimestamp()
                    )
                    ProfilesManager.saveProfile(updatedProfile)
                } else {
                    DialogsManager.setAlert(
                        "Invalid config\n\n$configError"
                    )
                }
            } catch (e: Exception) {
                DialogsManager.setLoading(false)
                DialogsManager.setAlert(
                    ("Request error" + "\n\n" + (e.message ?: "")).trim()
                )
            }
        }
    }

    fun showQRCode(profile: Profile) {
        val onDismiss = { DialogsManager.removeDialog(PROFILE_QR_SCREEN_ID) }
        DialogsManager.addDialog(
            id = PROFILE_QR_SCREEN_ID,
            onDismissRequest = onDismiss,
            content = {
                ProfileQRScreen(
                    profile = profile,
                    closeHandler = onDismiss
                )
            }
        )
    }

    private fun deleteProfile(profile: Profile) {
        ProfilesManager.deleteProfile(profile)
        if (SingBoxManager.lastStartedProfile?.id == profile.id) {
            SingBoxManager.stop()
        }
    }

}