package org.mikhailzhdanov.deskbox.modules.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mikhailzhdanov.deskbox.managers.AlertData
import org.mikhailzhdanov.deskbox.managers.DialogData
import org.mikhailzhdanov.deskbox.managers.DialogsManager

class DialogsViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(
        DialogsUIState(
            alertData = null,
            isLoading = false,
            dialogs = emptyList(),
            showConfigOverrideValueDialog = false,
            toastText = ""
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                DialogsManager.alertData,
                DialogsManager.isLoading,
                DialogsManager.dialogs,
                DialogsManager.showConfigOverrideValueDialog,
                DialogsManager.toastText
            ) { alertData, isLoading, dialogs, showConfigOverrideValueDialog, toastText ->
                ObservedValues(alertData, isLoading, dialogs, showConfigOverrideValueDialog, toastText)
            }.collect { values ->
                _uiState.update { current ->
                    current.copy(
                        alertData = values.alertData,
                        isLoading = values.isLoading,
                        dialogs = values.dialogs,
                        showConfigOverrideValueDialog = values.showConfigOverrideValueDialog,
                        toastText = values.toastText
                    )
                }
            }
        }
    }

    fun hideAlert() {
        DialogsManager.setAlert(null)
    }

    fun hideConfigOverrideValueDialog() {
        DialogsManager.setConfigOverrideValueDialog(false)
    }

}

private data class ObservedValues(
    val alertData: AlertData?,
    val isLoading: Boolean,
    val dialogs: List<DialogData>,
    val showConfigOverrideValueDialog: Boolean,
    val toastText: String
)