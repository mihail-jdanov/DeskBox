package org.mikhailzhdanov.deskbox.tools

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimestampFormatter {

    fun format(timestamp: Long): String {
        val instant = Instant.ofEpochSecond(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
        return dateTime.format(formatter)
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

}