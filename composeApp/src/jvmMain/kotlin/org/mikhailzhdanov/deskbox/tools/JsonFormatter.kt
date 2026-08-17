package org.mikhailzhdanov.deskbox.tools

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonFormatter {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        allowComments = true
        allowTrailingComma = true
    }

    fun formatJson(jsonString: String): String {
        try {
            val jsonElement = json.parseToJsonElement(jsonString)
            return json.encodeToString(jsonElement)
        } catch (e: Exception) {
            return jsonString
        }
    }

}