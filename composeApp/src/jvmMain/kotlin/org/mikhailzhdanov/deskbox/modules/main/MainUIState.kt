package org.mikhailzhdanov.deskbox.modules.main

import org.mikhailzhdanov.deskbox.Theme

data class MainUIState(
    val selectedTab: Int,
    val preferredTheme: Theme
)