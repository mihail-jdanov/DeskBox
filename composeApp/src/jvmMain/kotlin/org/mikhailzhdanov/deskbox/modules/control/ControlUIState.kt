package org.mikhailzhdanov.deskbox.modules.control

import org.mikhailzhdanov.deskbox.Profile

data class ControlUIState(
    val isRunning: Boolean,
    val version: String,
    val isStartAvailable: Boolean,
    val isVersionError: Boolean,
    val profiles: List<Profile>,
    val selectedProfile: Profile?,
    val showProfileDropdown: Boolean
)