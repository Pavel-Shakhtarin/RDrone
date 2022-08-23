package ru.pavel.shakhtarin.rdrone.server.rsocket.data

import ru.pavel.shakhtarin.rdrone.server.graphic.level.CellType

data class CellId(
    val seekerId: Long,
    val seeker: Seeker? = null,
    val type: CellType
)
