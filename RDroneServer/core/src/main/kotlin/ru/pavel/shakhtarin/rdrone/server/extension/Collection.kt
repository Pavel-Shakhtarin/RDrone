package ru.pavel.shakhtarin.rdrone.server.extension

import java.util.LinkedList

fun <T> List<T>.toLinkedList(): LinkedList<T> {
    return LinkedList(this)
}