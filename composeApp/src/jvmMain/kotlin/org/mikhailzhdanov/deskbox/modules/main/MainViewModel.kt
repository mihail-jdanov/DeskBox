package org.mikhailzhdanov.deskbox.modules.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.managers.AlertData
import org.mikhailzhdanov.deskbox.managers.AlertsManager
import org.mikhailzhdanov.deskbox.managers.ProfilesManager
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.String

class MainViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUIState(
            selectedTab = 0,
            alertData = null,
            isLoading = false
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                combine(
                    AlertsManager.alertData,
                    AlertsManager.isLoading
                ) { alertData, isLoading ->
                    ObservedValues(alertData, isLoading)
                }.collect { values ->
                    _uiState.update { current ->
                        current.copy(
                            alertData = values.alertData,
                            isLoading = values.isLoading
                        )
                    }
                }
            }
            launch {
                ProfilesManager.profileToImport.collect { profileToImport ->
                    profileToImport?.let {
                        setSelectedTab(1)
                    }
                }
            }
        }
    }

    fun setSelectedTab(selectedTab: Int) {
        _uiState.update { it.copy(selectedTab = selectedTab) }
    }

    fun hideAlert() {
        AlertsManager.setAlert(null)
    }

}

private data class ObservedValues(
    val alertData: AlertData?,
    val isLoading: Boolean
)