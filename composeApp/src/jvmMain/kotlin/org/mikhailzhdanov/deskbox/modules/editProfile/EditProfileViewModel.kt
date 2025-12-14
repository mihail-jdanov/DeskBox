package org.mikhailzhdanov.deskbox.modules.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.AlertsManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.tools.RemoteConfigsFetcher
import org.mikhailzhdanov.deskbox.managers.SingBoxManager
import org.mikhailzhdanov.deskbox.tools.JsonFormatter
import org.mikhailzhdanov.deskbox.tools.TimestampFormatter

class EditProfileViewModel(
    val profile: Profile,
    val saveSuccessHandler: () -> Unit
): ViewModel() {

    private val _uiState = MutableStateFlow(
        EditProfileUIState(
            profile = profile,
            showConfigDialog = false,
            isSaveAvailable = false,
            isConfigInvalid = false,
            configButtonMode = getConfigButtonMode(profile.isRemote)
        )
    )

    private val nameMaxChars = 200
    private val defaultConfig = "{}"
    private val defaultAutoUpdateInterval: Long = 60

    val uiState = _uiState.asStateFlow()

    val isEditingExistingProfile: Boolean
        get() {
            return ProfilesManager.profiles.value.contains(profile)
        }

    val invalidConfigTitle = "Invalid config"

    init {
        if (profile.config.isEmpty()) {
            setConfig(defaultConfig)
        }
        if (profile.autoUpdateInterval == null) {
            setAutoUpdateInterval(defaultAutoUpdateInterval)
        }
    }

    fun setName(name: String) {
        val name = name.take(nameMaxChars)
        _uiState.update { it.copy(profile = it.profile.copy(name = name)) }
        validateProfile()
    }

    fun setIsRemote(isRemote: Boolean) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(isRemote = isRemote),
                configButtonMode = getConfigButtonMode(isRemote)
            )
        }
        validateProfile()
    }

    fun setConfig(config: String) {
        _uiState.update { it.copy(profile = it.profile.copy(config = config)) }
    }

    fun setRemoteURL(remoteURL: String) {
        _uiState.update { it.copy(profile = it.profile.copy(remoteURL = remoteURL)) }
        validateProfile()
    }

    fun setAutoUpdate(autoUpdate: Boolean) {
        _uiState.update { it.copy(profile = it.profile.copy(autoUpdate = autoUpdate)) }
        validateProfile()
    }

    fun setAutoUpdateInterval(autoUpdateInterval: Long?) {
        _uiState.update {
            it.copy(profile = it.profile.copy(autoUpdateInterval = autoUpdateInterval))
        }
        validateProfile()
    }

    fun showConfigDialog() {
        _uiState.update { it.copy(showConfigDialog = true) }
    }

    fun hideConfigDialog() {
        _uiState.update { it.copy(showConfigDialog = false) }
        setConfig(JsonFormatter.formatJson(_uiState.value.profile.config))
        validateConfig()
        validateProfile()
    }

    fun showConfigErrorAlert() {
        val configError = SingBoxManager.validateConfig(
            _uiState.value.profile.config
        )
        AlertsManager.setAlert(invalidConfigTitle + "\n\n" + configError)
    }

    fun save() {
        fun getProfile(): Profile { return _uiState.value.profile }
        if (getProfile().isRemote) {
            val isURLChanged = getProfile().remoteURL != profile.remoteURL
            if (isURLChanged || profile.config.isEmpty()) {
                AlertsManager.setLoading(true)
                viewModelScope.launch {
                    try {
                        val config = RemoteConfigsFetcher.fetchConfig(
                            getProfile().remoteURL
                        )
                        AlertsManager.setLoading(false)
                        val configError = SingBoxManager.validateConfig(config)
                        if (configError.isEmpty()) {
                            setConfig(config)
                            setLastUpdateTimestamp(
                                TimestampFormatter.getCurrentTimestamp()
                            )
                            completeSaving()
                        } else {
                            AlertsManager.setAlert(
                                invalidConfigTitle + "\n\n" + configError
                            )
                        }
                    } catch (e: Exception) {
                        AlertsManager.setLoading(false)
                        AlertsManager.setAlert(e.message ?: "")
                    }
                }
            } else {
                setConfig(profile.config)
                completeSaving()
            }
        } else {
            completeSaving()
        }
    }

    private fun completeSaving() {
        var newProfile = _uiState.value.profile
        if (newProfile.isRemote) {
            if (!newProfile.autoUpdate) {
                newProfile = newProfile.copy(autoUpdateInterval = defaultAutoUpdateInterval)
            }
        } else {
            newProfile = newProfile.copy(
                remoteURL = "",
                autoUpdate = true,
                autoUpdateInterval = defaultAutoUpdateInterval,
                lastUpdateTimestamp = 0
            )
        }
        ProfilesManager.saveProfile(newProfile)
        saveSuccessHandler()
    }

    private fun setLastUpdateTimestamp(timestamp: Long) {
        _uiState.update {
            it.copy(profile = it.profile.copy(lastUpdateTimestamp = timestamp))
        }
    }

    private fun validateProfile() {
        val profile = _uiState.value.profile
        var isValid = profile.name.isNotEmpty()
        if (profile.isRemote) {
            if (profile.remoteURL.isEmpty()) isValid = false
            if (profile.autoUpdate) {
                if ((profile.autoUpdateInterval ?: 0) <= 0) isValid = false
            }
        } else {
            if (_uiState.value.isConfigInvalid) {
                isValid = false
            }
        }
        _uiState.update { it.copy(isSaveAvailable = isValid) }
    }

    private fun validateConfig() {
        val config = _uiState.value.profile.config
        val isValid = SingBoxManager.isValidConfig(config)
        _uiState.update { it.copy(isConfigInvalid = !isValid) }
    }

    private fun getConfigButtonMode(isRemote: Boolean): ConfigButtonMode {
        return if (!isRemote) {
            ConfigButtonMode.Edit
        } else if (profile.remoteURL.isNotEmpty() && profile.config.isNotEmpty()) {
            ConfigButtonMode.View
        } else {
            ConfigButtonMode.None
        }
    }

}