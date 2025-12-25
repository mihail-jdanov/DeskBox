package org.mikhailzhdanov.deskbox.modules.editProfile

import org.mikhailzhdanov.deskbox.Profile

data class EditProfileUIState(
    val profile: Profile,
    val isSaveAvailable: Boolean,
    val isConfigInvalid: Boolean,
    val configButtonMode: ConfigButtonMode
)

enum class ConfigButtonMode {
    Edit,
    View,
    None
}