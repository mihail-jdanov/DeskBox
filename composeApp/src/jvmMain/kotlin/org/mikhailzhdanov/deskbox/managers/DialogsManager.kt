package org.mikhailzhdanov.deskbox.managers

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object DialogsManager {

    private const val TOAST_TIMEOUT = 2000L

    private val scope = CoroutineScope(Dispatchers.Main)
    private val _alertData = MutableStateFlow<AlertData?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _dialogs = MutableStateFlow(emptyList<DialogData>())
    private val _showConfigOverrideValueDialog = MutableStateFlow(false)
    private val _toastText = MutableStateFlow("")

    private var toastResetJob: Job? = null

    val alertData = _alertData.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val dialogs = _dialogs.asStateFlow()
    val showConfigOverrideValueDialog = _showConfigOverrideValueDialog.asStateFlow()
    val toastText = _toastText.asStateFlow()

    fun setAlert(text: String) {
        _alertData.value = AlertData(text = text)
    }

    fun setAlert(alertData: AlertData?) {
        _alertData.value = alertData
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun addDialog(
        id: String,
        onDismissRequest: () -> Unit,
        content: @Composable () -> Unit
    ) {
        removeDialog(id)
        _dialogs.value += DialogData(id, onDismissRequest, content)
    }

    fun removeDialog(id: String) {
        _dialogs.update { dialogs ->
            dialogs.filter { it.id != id }
        }
    }

    fun setConfigOverrideValueDialog(isVisible: Boolean) {
        _showConfigOverrideValueDialog.value = isVisible
    }

    fun setToastText(text: String) {
        _toastText.value = text
        toastResetJob?.cancel()
        if (text.trim().isNotEmpty()) {
            toastResetJob = scope.launch {
                delay(TOAST_TIMEOUT)
                _toastText.value = ""
            }
        }
    }

}

data class AlertData(
    val text: String,
    val confirmButtonData: AlertButtonData = AlertButtonData.ok(),
    val cancelButtonData: AlertButtonData? = null
)

data class AlertButtonData(
    val title: String,
    val handler: () -> Unit
) {
    companion object {
        fun ok(handler: (() -> Unit)? = null): AlertButtonData {
            return AlertButtonData(
                title = "OK",
                handler = handler ?: {}
            )
        }

        fun cancel(): AlertButtonData {
            return AlertButtonData(
                title = "Cancel",
                handler = {}
            )
        }
    }
}
data class DialogData(
    val id: String,
    val onDismissRequest: () -> Unit,
    val content: @Composable () -> Unit
)