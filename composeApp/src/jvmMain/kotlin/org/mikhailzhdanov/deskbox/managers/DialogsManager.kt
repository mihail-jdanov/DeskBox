package org.mikhailzhdanov.deskbox.managers

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object DialogsManager {

    private val _alertData: MutableStateFlow<AlertData?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _dialogs = MutableStateFlow(emptyList<DialogData>())

    val alertData = _alertData.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val dialogs = _dialogs.asStateFlow()

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