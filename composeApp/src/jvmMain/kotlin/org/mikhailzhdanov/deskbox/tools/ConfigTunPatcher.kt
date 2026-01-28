package org.mikhailzhdanov.deskbox.tools

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import kotlin.collections.set

object ConfigTunPatcher {

    fun patchTunInterfaceName(os: OS, configJson: String): String {
        if (os.type == OSType.Linux) return configJson
        val json = JsonFormatter.json
        val config = json.parseToJsonElement(configJson)
        val configObj = config.jsonObject
        val inbounds = configObj["inbounds"]?.jsonArray ?: return configJson
        val patchedInbounds = inbounds.map { inbound ->
            val inboundObj = inbound.jsonObject
            if (inboundObj["type"]?.jsonPrimitive?.content == "tun") {
                val originalName = inboundObj["interface_name"]?.jsonPrimitive?.content ?: "tun"
                val newName = generateTunInterfaceName(
                    os = os,
                    baseName = originalName
                )
                inboundObj.toMutableMap().apply {
                    this["interface_name"] = JsonPrimitive(newName)
                }.let { json.encodeToJsonElement(it) }
            } else {
                inbound
            }
        }
        val patchedConfig = configObj.toMutableMap().apply {
            this["inbounds"] = JsonArray(patchedInbounds)
        }
        return json.encodeToString(
            JsonElement.serializer(),
            JsonObject(patchedConfig)
        )
    }

    private fun generateTunInterfaceName(os: OS, baseName: String): String {
        when (os.type) {
            OSType.Windows -> {
                if (!os.isOldOS) return "singtun0"
                val randomChars = UUID.randomUUID().toString()
                    .replace("-", "")
                    .take(4)
                    .uppercase()
                return "$baseName-$randomChars"
            }
            OSType.MacOS -> {
                return "utun123"
            }
            OSType.Linux -> {
                return baseName
            }
        }
    }

}