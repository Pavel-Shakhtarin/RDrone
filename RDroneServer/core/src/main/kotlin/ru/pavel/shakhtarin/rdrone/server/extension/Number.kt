package ru.pavel.shakhtarin.rdrone.server.extension

import kotlin.math.ceil

fun Int.resizeBy(k: Int, ceil: Boolean = true): Int {
    return if (ceil) this - (ceil(this.toFloat() / k).toInt())
    else this - (this / k)
}