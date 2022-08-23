package ru.pavel.shakhtarin.rdrone.server.graphic.level

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import ru.pavel.shakhtarin.rdrone.server.graphic.model.ModelHandler
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.CellId
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Point
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Seeker
import kotlin.math.roundToInt

class CellRenderer {

    private val targetSize = 0.4f
    private val trackSize = 0.3f
    private val obstacleSize = 0.1f
    private val visionOffset = 0.5f
    private val attributes = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()

    private fun targetModel(modelBuilder: ModelBuilder) = modelBuilder.createBox(
        targetSize, targetSize, targetSize,
        Material(ColorAttribute.createDiffuse(Color.GOLD)),
        attributes
    )

    private fun obstacleModel(modelBuilder: ModelBuilder, size: Float = obstacleSize) = modelBuilder.createBox(
        size, size, size,
        Material(ColorAttribute.createDiffuse(Color.BROWN)),
        attributes
    )

    private fun pathModel(modelBuilder: ModelBuilder, color: Color): Model {
        return modelBuilder.createBox(
            trackSize, trackSize, trackSize,
            Material(ColorAttribute.createDiffuse(color)),
            attributes
        )
    }

    fun reRenderCells(
        modelHandler: ModelHandler,
        world: World,
        seeker: Seeker,
        vision: Array<Array<MutableList<Point>>>
    ) {
        val seekerObjects = modelHandler.seekerInstances[seeker] ?: mutableListOf()
        val obstacleModel = obstacleModel(modelHandler.modelBuilder)
        val obstacleShellModel = obstacleModel(modelHandler.modelBuilder, 1f)
        val targetModel = targetModel(modelHandler.modelBuilder)
        val pathModel = pathModel(modelHandler.modelBuilder, seeker.color)
        seekerObjects.clear()
        vision.forEachIndexed { x, arr ->
            arr.forEachIndexed { y, list ->
                list.forEachIndexed { z, point ->
                    val notSeeker = point.position.toVector3() != seeker.currentPosition
                    val position = world.seekerVision(seeker.currentPosition)[x][y][z].position
                    val xVd = position.x.toFloat() + visionOffset
                    val yVd = position.y.toFloat() + visionOffset
                    val zVd = position.z.toFloat() + visionOffset
                    val vector = Vector3(xVd, yVd, zVd)
                    if (point.cost != 0 && notSeeker) {
                        seekerObjects += ModelInstance(obstacleModel).apply {
                            userData = CellId(seeker.id, null, CellType.OBSTACLE)
                            transform.translate(vector)
                            transform.scl(point.cost.toFloat())
                        }
                        seekerObjects += ModelInstance(obstacleShellModel).apply {
                            userData = CellId(seeker.id, null, CellType.OBSTACLE)
                            transform.translate(vector)
                            val material = materials.first()
                            material.set(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
                            material.set(ColorAttribute.createDiffuse(255f, 255f, 255f, 0.3f))
                        }
                    }
                    if (point.event != null && notSeeker) {
                        seekerObjects += ModelInstance(targetModel).apply {
                            userData = CellId(seeker.id, null, CellType.TARGET)
                            transform.translate(vector)
                        }
                    }
                }
            }
        }
        seeker.path.forEach { position ->
            val point = world.worldMatrix[position.x.roundToInt()][position.y.roundToInt()][position.z.roundToInt()]
            if (point.event == null) {
                seekerObjects += ModelInstance(pathModel).apply {
                    userData = CellId(seeker.id, null, CellType.PATH)
                    val xVd = position.x + visionOffset
                    val yVd = position.y + visionOffset
                    val zVd = position.z + visionOffset
                    val vector = Vector3(xVd, yVd, zVd)
                    transform.translate(vector)
                }
            }
        }
        modelHandler.seekerInstances[seeker] = seekerObjects
    }


}