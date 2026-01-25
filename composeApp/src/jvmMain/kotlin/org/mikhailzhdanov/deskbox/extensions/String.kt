package org.mikhailzhdanov.deskbox.extensions

fun String.isValidIPv4(): Boolean {
    val regex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    return regex.matches(this)
}

fun String.isValidIPv6(): Boolean {
    val regex = Regex("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")
    return regex.matches(this)
}

fun String.isValidIP(): Boolean {
    return isValidIPv4() || isValidIPv6()
}