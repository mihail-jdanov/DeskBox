package org.mikhailzhdanov.deskbox.modules.configOverrideValue

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mikhailzhdanov.deskbox.managers.SettingsManager

class ConfigOverrideValueViewModel(
    val closeHandler: () -> Unit
): ViewModel() {

    private val _uiState = MutableStateFlow(
        ConfigOverrideValueUIState(
            overrideText = SettingsManager.configOverrideValue.value
        )
    )

    val uiState = _uiState.asStateFlow()

    fun setOverrideText(text: String) {
        _uiState.update { it.copy(overrideText = text) }
    }

    fun saveOverrideText() {
        SettingsManager.setConfigOverrideValue(
            _uiState.value.overrideText.trim()
        )
    }

}