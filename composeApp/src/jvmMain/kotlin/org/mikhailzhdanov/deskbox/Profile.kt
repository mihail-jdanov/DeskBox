package org.mikhailzhdanov.deskbox

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val isRemote: Boolean = false,
    val remoteURL: String = "",
    val autoUpdate: Boolean = true,
    val autoUpdateInterval: Long? = null,
    val lastUpdateTimestamp: Long = 0,
    val config: String = ""
)