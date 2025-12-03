package org.mikhailzhdanov.deskbox.modules.appContainer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppContainerViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        AppContainerUIState(
            isLoading = false,
            alertText = ""
        )
    )

    val uiState = _uiState.asStateFlow()

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun setAlertText(alertText: String?) {
        _uiState.update { it.copy(alertText = alertText ?: "") }
    }

}