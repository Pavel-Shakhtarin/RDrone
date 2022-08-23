package ru.pavel.shakhtarin.rdrone.server.rsocket.data

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import java.util.LinkedList


data class Seeker(
    val id: Long,
    val name: String,
    var currentPosition: Vector3,
    val color: Color,
    var visionMatrix: Array<Array<MutableList<Point>>>,
    val movementK: Float,
    var moving: Boolean = false,
    var nextStep: Vector3 = Vector3(0.0f, 0.0f, 0.0f),
    var direction: Vector3 = nextStep.sub(currentPosition).nor(),
    var path: LinkedList<Vector3> = LinkedList()
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Seeker

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}