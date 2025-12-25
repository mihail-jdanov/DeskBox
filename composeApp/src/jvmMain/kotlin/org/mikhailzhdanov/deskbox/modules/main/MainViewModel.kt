package org.mikhailzhdanov.deskbox.modules.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Theme
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import org.mikhailzhdanov.deskbox.managers.SettingsManager

class MainViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUIState(
            selectedTab = 0,
            preferredTheme = Theme.fromRawValue(
                SettingsManager.preferredTheme.value
            )
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                ProfilesManager.profileToImport.collect { profileToImport ->
                    profileToImport?.let {
                        setSelectedTab(1)
                    }
                }
            }
            launch {
                SettingsManager.preferredTheme.collect { preferredTheme ->
                    _uiState.update {
                        it.copy(
                            preferredTheme = Theme.fromRawValue(preferredTheme)
                        )
                    }
                }
            }
        }
    }

    fun setSelectedTab(selectedTab: Int) {
        _uiState.update { it.copy(selectedTab = selectedTab) }
    }

    fun switchTheme() {
        val newTheme = _uiState.value.preferredTheme.getNext()
        SettingsManager.setPreferredTheme(newTheme.rawValue)
    }

}