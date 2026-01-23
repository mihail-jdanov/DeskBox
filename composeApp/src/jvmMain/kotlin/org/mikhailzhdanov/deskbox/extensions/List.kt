package org.mikhailzhdanov.deskbox.extensions

fun <T> List<T>.toFixedSize(size: Int): List<T?> {
    val result = mutableListOf<T?>()
    result.addAll(this)
    while (result.size < size) {
        result.add(null)
    }
    return result.take(size)
}