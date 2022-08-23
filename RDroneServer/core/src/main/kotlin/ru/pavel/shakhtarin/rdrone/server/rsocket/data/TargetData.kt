package ru.pavel.shakhtarin.rdrone.server.rsocket.data

import ru.pavel.shakhtarin.rdrone.server.rsocket.data.RSocketData

data class TargetData(
    val timestamp: Long,
    val lat: Double,
    val lng: Double
) : RSocketData
