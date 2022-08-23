package ru.pavel.shakhtarin.rdrone.server.rsocket.data


data class InitRequest(
    val id: Long,
    val name: String
) : RSocketData
