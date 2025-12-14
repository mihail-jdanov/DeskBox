package org.mikhailzhdanov.deskbox.tools

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonFormatter {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun formatJson(jsonString: String): String {
        try {
            val jsonElement = Json.parseToJsonElement(jsonString)
            return json.encodeToString(jsonElement)
        } catch (e: Exception) {
            return jsonString
        }
    }

}