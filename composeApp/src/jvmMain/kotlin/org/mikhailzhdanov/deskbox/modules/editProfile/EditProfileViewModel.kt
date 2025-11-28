package org.mikhailzhdanov.deskbox.modules.editProfile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SingBoxManager

class EditProfileViewModel(
    val profile: Profile,
    val saveSuccessHandler: () -> Unit
): ViewModel() {

    private val _uiState = MutableStateFlow(
        EditProfileUIState(
            profile = profile,
            showEditConfigDialog = false,
            isSaveAvailable = false,
            errorMessage = ""
        )
    )

    private val nameMaxChars = 200

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    val uiState = _uiState.asStateFlow()

    val isEditingExistingProfile: Boolean
        get() {
            return ProfilesManager.profiles.value.contains(profile)
        }

    init {
        if (profile.config.isEmpty()) {
            setConfig("{}")
        }
    }

    fun setName(name: String) {
        val name = name.take(nameMaxChars)
        _uiState.update { it.copy(profile = it.profile.copy(name = name)) }
        validateProfile()
    }

    fun setIsRemote(isRemote: Boolean) {
        _uiState.update { it.copy(profile = it.profile.copy(isRemote = isRemote)) }
        validateProfile()
    }

    fun setConfig(config: String) {
        _uiState.update { it.copy(profile = it.profile.copy(config = config)) }
        validateProfile()
    }

    fun showEditConfigDialog() {
        _uiState.update { it.copy(showEditConfigDialog = true) }
    }

    fun hideEditConfigDialog() {
        _uiState.update { it.copy(showEditConfigDialog = false) }
        formatConfig()
    }

    fun save() {
        val newProfile = _uiState.value.profile
        val oldProfile = ProfilesManager.profiles.value.firstOrNull { it.id == newProfile.id }
        ProfilesManager.saveProfile(newProfile)
        saveSuccessHandler()
        oldProfile?.let { oldProfile ->
            if (SingBoxManager.isRunning.value
                && SingBoxManager.lastStartedProfile?.id == newProfile.id
                && oldProfile.config != newProfile.config) {
                SingBoxManager.stop()
                SingBoxManager.start(newProfile)
            }
        }
    }

    private fun validateProfile() {
        val profile = _uiState.value.profile
        setErrorMessage("")
        var isValid = profile.name.isNotEmpty() && profile.config.isNotEmpty()
        if (!profile.isValidConfig()) {
            isValid = false
            setErrorMessage("Invalid config JSON")
        }
        _uiState.update { it.copy(isSaveAvailable = isValid) }
    }

    private fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun formatConfig() {
        try {
            val config = _uiState.value.profile.config
            val jsonElement = Json.parseToJsonElement(config)
            val formattedConfig = json.encodeToString(jsonElement)
            setConfig(formattedConfig)
        } catch (e: Exception) {
            // Do nothing
        }
    }

}