package org.mikhailzhdanov.deskbox.modules.dialogs

import org.mikhailzhdanov.deskbox.managers.AlertData
import org.mikhailzhdanov.deskbox.managers.DialogData

data class DialogsUIState(
    val alertData: AlertData?,
    val isLoading: Boolean,
    val dialogs: List<DialogData>,
    val showConfigOverrideValueDialog: Boolean,
    val toastText: String
)