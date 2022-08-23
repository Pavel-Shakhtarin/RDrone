package ru.pavel.shakhtarin.rdrone.client.rsocket.data

import ru.pavel.shakhtarin.rdrone.client.rsocket.RSocketData

data class TargetData(
    val timestamp: Long,
    val lat: Double,
    val lng: Double
) : RSocketData
