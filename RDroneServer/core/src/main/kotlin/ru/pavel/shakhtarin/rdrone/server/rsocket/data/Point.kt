package ru.pavel.shakhtarin.rdrone.server.rsocket.data

data class Point(
    val position: Vector3D,
    val otherSeeker: Seeker?,
    var event: Event? = null,
    val cost: Int
)

data class Event(
    val timestamp: Long?,
    val lat: Double,
    val lng: Double
)