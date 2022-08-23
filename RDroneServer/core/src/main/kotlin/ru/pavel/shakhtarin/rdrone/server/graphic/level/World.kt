package ru.pavel.shakhtarin.rdrone.server.graphic.level

import com.badlogic.gdx.math.Vector3
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Event
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Point
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Seeker
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Vector3D
import ru.pavel.shakhtarin.rdrone.server.rsocket.service.PathFinder
import io.rsocket.Payload
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import java.util.*
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random


class World {

    private val logger = LoggerFactory.getLogger(javaClass)

    val size: Int = 72//48
    private val pathFinder = PathFinder(size)
    private val visionRange = 3
    val worldMatrix = init3DArray(size)
    val seekers = mutableListOf<Seeker>()
    val emitterMap = mutableMapOf<Long, FluxSink<Payload>>()
    private val maxTargets = (size.toDouble().pow(3.0) / 800).toInt()
    private var currentTargets = 0
    private val spawnStart: Int = 0
    private val spawnEnd: Int = size
    private val colorQ = colorQ()

    private fun init3DArray(sizeX: Int, sizeY: Int = sizeX, sizeZ: Int = sizeX) =
        Array(sizeX) {
            Array(sizeY) {
                MutableList<Point>(sizeZ) {
                    Point(Vector3D(0, 0, 0), null, null, 0)
                }
            }
        }

    init {
        worldMatrix.forEachIndexed { x, arr: Array<MutableList<Point>> ->
            arr[x].forEachIndexed { y, _ ->
                (0 until size).forEach { z ->
                    val costBase = Random.nextInt(1, 256)
                    val cost = if (costBase % 12 == 0) Random.nextInt(1, 5) else 0
                    val event = if (cost == 0 && currentTargets <= maxTargets && costBase % 137 == 0) {
                        Event(System.currentTimeMillis(), 55.5, 55.7)
                    } else null
                    worldMatrix[x][y][z] = Point(Vector3D(x, y, z), null, event, cost)
                }
            }
        }
        (0..15).forEach { _ ->
            seekers += addSeeker(System.currentTimeMillis())
        }
    }

    private fun randomSpawn() =
        Vector3D(
            Random.nextInt(spawnStart, spawnEnd),
            Random.nextInt(spawnStart, spawnEnd),
            Random.nextInt(spawnStart, spawnEnd)
        )

    fun seekerVision(position: Vector3): Array<Array<MutableList<Point>>> {
        val p3D = Vector3D(position.x.toInt(), position.y.toInt(), position.z.toInt())
        val visionXR = min(visionRange, size - 1 - p3D.x)
        val visionYR = min(visionRange, size - 1 - p3D.y)
        val visionZR = min(visionRange, size - 1 - p3D.z)
        val visionXL = min(visionRange, p3D.x)
        val visionYL = min(visionRange, p3D.y)
        val visionZL = min(visionRange, p3D.z)
        val offsetX = visionXR + visionXL + 1
        val offsetY = visionYR + visionYL + 1
        val offsetZ = visionZR + visionZL + 1
        val vision = init3DArray(offsetX, offsetY, offsetZ)
        val rangeX = (p3D.x - visionXL..p3D.x + visionXR)
        val rangeY = (p3D.y - visionYL..p3D.y + visionYR)
        val rangeZ = (p3D.z - visionZL..p3D.z + visionZR)
        var xV = 0
        var yV = 0
        var zV = 0
        rangeX.forEach { x ->
            rangeY.forEach { y ->
                rangeZ.forEach { z ->
                    vision[xV][yV][zV] = worldMatrix[x][y][z]
                    zV++
                }
                zV = 0
                yV++
            }
            yV = 0
            xV++
        }
        return vision
    }

    fun path(position: Vector3, vision: Array<Array<MutableList<Point>>>): LinkedList<Vector3> {
        val seekerPosition = Vector3D(position.x.toInt(), position.y.toInt(), position.z.toInt())
        return pathFinder.calculatePath(seekerPosition, vision)
    }

    fun addSeeker(id: Long): Seeker {
        val seekerPosition = randomSpawn()
        val vision = seekerVision(seekerPosition.toVector3())
        val color = colorQ.poll()
        val vector3 = seekerPosition.toVector3()
        return Seeker(
            id = id,
            name = "${Random.nextLong(0, Long.MAX_VALUE)}",
            currentPosition = vector3,
            color = color.color,
            visionMatrix = vision,
            movementK = 0.01f,
            path = pathFinder.calculatePath(seekerPosition, vision)
        )
    }

}