package org.mikhailzhdanov.deskbox.modules.main

import org.mikhailzhdanov.deskbox.managers.AlertData

data class MainUIState(
    val selectedTab: Int,
    val alertData: AlertData?,
    val isLoading: Boolean
)