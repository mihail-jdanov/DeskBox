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
            dialogs = emptyList()
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                DialogsManager.alertData,
                DialogsManager.isLoading,
                DialogsManager.dialogs
            ) { alertData, isLoading, dialogs ->
                ObservedValues(alertData, isLoading, dialogs)
            }.collect { values ->
                _uiState.update { current ->
                    current.copy(
                        alertData = values.alertData,
                        isLoading = values.isLoading,
                        dialogs = values.dialogs
                    )
                }
            }
        }
    }

    fun hideAlert() {
        DialogsManager.setAlert(null)
    }

}

private data class ObservedValues(
    val alertData: AlertData?,
    val isLoading: Boolean,
    val dialogs: List<DialogData>
)