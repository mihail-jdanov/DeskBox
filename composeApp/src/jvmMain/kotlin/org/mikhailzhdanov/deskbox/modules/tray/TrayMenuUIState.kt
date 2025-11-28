package org.mikhailzhdanov.deskbox.modules.tray

import org.jetbrains.compose.resources.DrawableResource

data class TrayMenuUIState(
    val icon: DrawableResource,
    val items: List<TrayMenuItem>
)

data class TrayMenuItem(
    val title: String,
    val checked: Boolean,
    val enabled: Boolean,
    val onClick: () -> Unit
)