package org.mikhailzhdanov.deskbox

enum class Theme(
    val rawValue: Int
) {
    Auto(0),
    Light(1),
    Dark(2);

    fun getNext(): Theme {
        return when (this) {
            Auto -> Light
            Light -> Dark
            Dark -> Auto
        }
    }

    companion object {
        fun fromRawValue(rawValue: Int): Theme {
            return Theme.entries.firstOrNull { it.rawValue == rawValue } ?: Auto
        }
    }
}