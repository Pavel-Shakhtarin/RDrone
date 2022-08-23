package ru.pavel.shakhtarin.rdrone.server.rsocket.data

import com.badlogic.gdx.math.Vector3
import kotlin.math.pow
import kotlin.math.sqrt

data class Vector3D(
    val x: Int,
    val y: Int,
    val z: Int
) : RSocketData {

    fun toVector3() = Vector3(x.toFloat(), y.toFloat(), z.toFloat())

    fun distance(target: Vector3D): Double {
        val xSqr = (x - target.x).toDouble().pow(2)
        val ySqr = (y - target.y).toDouble().pow(2)
        val zSqr = (z - target.z).toDouble().pow(2)
        return sqrt(xSqr + ySqr + zSqr)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3D

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

}
