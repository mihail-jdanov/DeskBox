package org.mikhailzhdanov.deskbox

import java.util.UUID

data class LogLine(
    val id: UUID = UUID.randomUUID(),
    val value: String
)