package ru.pavel.shakhtarin.rdrone.server.rsocket.exception

import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Vector3D

class NoPathException(currentPosition: Vector3D) : Exception("No path fount for: $currentPosition") {
}