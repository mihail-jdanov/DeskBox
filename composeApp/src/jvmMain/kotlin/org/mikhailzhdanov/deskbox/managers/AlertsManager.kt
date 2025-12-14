package org.mikhailzhdanov.deskbox.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AlertsManager {

    private val _alertData: MutableStateFlow<AlertData?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)

    val alertData = _alertData.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    fun setAlert(text: String) {
        _alertData.value = AlertData(text = text)
    }

    fun setAlert(alertData: AlertData?) {
        _alertData.value = alertData
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
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