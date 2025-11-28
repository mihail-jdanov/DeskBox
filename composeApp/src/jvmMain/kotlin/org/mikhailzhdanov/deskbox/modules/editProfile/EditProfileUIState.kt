package org.mikhailzhdanov.deskbox.modules.editProfile

import org.mikhailzhdanov.deskbox.Profile

data class EditProfileUIState(
    val profile: Profile,
    val showEditConfigDialog: Boolean,
    val isSaveAvailable: Boolean,
    val errorMessage: String
)