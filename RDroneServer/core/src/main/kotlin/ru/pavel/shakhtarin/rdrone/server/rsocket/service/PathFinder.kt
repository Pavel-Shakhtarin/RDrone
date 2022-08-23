package ru.pavel.shakhtarin.rdrone.server.rsocket.service

import com.badlogic.gdx.math.Vector3
import ru.pavel.shakhtarin.rdrone.server.extension.toLinkedList
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Point
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Vector3D
import ru.pavel.shakhtarin.rdrone.server.rsocket.exception.NoPathException
import java.util.*
import kotlin.random.Random

class PathFinder(private val worldSize: Int) {

    fun calculatePath(currentPosition: Vector3D, matrix: Array<Array<MutableList<Point>>>): LinkedList<Vector3> {
        val targets = findTargets(matrix)
        return if (targets.isNotEmpty()) pathToClosestTarget(currentPosition, targets, matrix)
        else pathToRandomPosition(currentPosition, matrix)
    }

    private fun findTargets(matrix: Array<Array<MutableList<Point>>>): List<Point> {
        val targets = mutableListOf<Point>()
        matrix.forEach { arr ->
            arr.forEach { points -> points.filter { point -> point.event != null }.forEach { targets += it } }
        }
        return targets.toList()
    }

    private fun pathToClosestTarget(
        currentPosition: Vector3D,
        targets: List<Point>,
        matrix: Array<Array<MutableList<Point>>>
    ): LinkedList<Vector3> {
        val paths = targets
            .map { target -> pathToTarget(currentPosition, target.position, matrix) }
        return closestTarget(currentPosition, paths).map { it.toVector3() }.toLinkedList()
    }

    private fun closestTarget(currentPosition: Vector3D, paths: List<List<Vector3D>>): List<Vector3D> {
        return paths.minByOrNull { it.size } ?: throw NoPathException(currentPosition)
    }

    private fun pathToTarget(
        position: Vector3D,
        target: Vector3D,
        matrix: Array<Array<MutableList<Point>>>
    ): List<Vector3D> {
        val path = mutableListOf<Vector3D>()
        var currentPosition = position
        do {
            val validDirections =
                orthogonalDirections(currentPosition)
            val excludePrevious = validDirections.filter { it !in path }
            val lessCostPoints = lessCostDirection(excludePrevious, matrix)
            val nextStep = step(target, lessCostPoints)
            currentPosition = Vector3D(nextStep.x, nextStep.y, nextStep.z)
            path += nextStep
        } while (nextStep != target)
        return path
    }

    private fun lessCostDirection(
        directions: List<Vector3D>,
        matrix: Array<Array<MutableList<Point>>>
    ): List<Point> {
        val matchingPoints = mutableListOf<Point>()
        matrix.forEach { arr ->
            arr.forEach { points ->
                points.forEach { point ->
                    val direction = directions.firstOrNull { it == point.position }
                    if (direction != null) matchingPoints += point
                }
            }
        }
        val directionsCost = matchingPoints.sortedBy { it.cost }
        return directionsCost.filter { it.cost == directionsCost.first().cost }
    }

    private fun orthogonalDirections(position: Vector3D): List<Vector3D> {
        val x = position.x
        val y = position.y
        val z = position.z
        val range = (0 until worldSize)
        return listOf(
            Vector3D(x, y + 1, z),
            Vector3D(x, y - 1, z),
            Vector3D(x + 1, y, z),
            Vector3D(x - 1, y, z),
            Vector3D(x, y, z + 1),
            Vector3D(x, y, z - 1)
        ).filter { it.x in range && it.y in range && it.z in range }
    }

    private fun step(target: Vector3D, lessCostPoints: List<Point>): Vector3D {
        return lessCostPoints.minByOrNull { it.position.distance(target) }!!.position
    }

    private fun pathToRandomPosition(
        currentPosition: Vector3D,
        matrix: Array<Array<MutableList<Point>>>
    ): LinkedList<Vector3> {
        val target = randomTarget(currentPosition, matrix)
        return pathToTarget(currentPosition, target, matrix).map { it.toVector3() }.toLinkedList()
    }

    private fun randomTarget(currentPosition: Vector3D, matrix: Array<Array<MutableList<Point>>>): Vector3D {
        val min = 0
        val rangeX = (min until matrix.size)
        val rangeY = (min until matrix.first().size)
        val rangeZ = (min until matrix.first().first().size)
        var targetPoint: Point
        do {
            val random = Random.nextInt(0, 6)
            val sideX = rangeX.random()
            val sideY = rangeY.random()
            val sideZ = rangeZ.random()
            val targetVector = when (random) {
                0 -> Vector3D(min, sideY, sideZ)
                1 -> Vector3D(sideX, sideY, sideZ)
                2 -> Vector3D(sideX, min, sideZ)
                3 -> Vector3D(sideX, sideY, sideZ)
                4 -> Vector3D(sideX, sideY, min)
                5 -> Vector3D(sideX, sideY, sideZ)
                else -> Vector3D(min, min, min)
            }
            targetPoint = matrix[targetVector.x][targetVector.y][targetVector.z]
        } while (targetPoint.cost != 0 && targetPoint.position != currentPosition)
        return targetPoint.position
    }

}