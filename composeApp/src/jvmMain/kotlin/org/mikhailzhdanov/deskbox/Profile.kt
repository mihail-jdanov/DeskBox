package org.mikhailzhdanov.deskbox

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val isRemote: Boolean = false,
    val remoteUrl: String = "",
    val config: String = ""
) {
    fun isValidConfig(): Boolean {
        return try {
            Json.parseToJsonElement(config)
            true
        } catch (e: Exception) {
            false
        }
    }
}