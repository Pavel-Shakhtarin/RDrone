package ru.pavel.shakhtarin.rdrone.server

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import ru.pavel.shakhtarin.rdrone.server.graphic.level.CellRenderer
import ru.pavel.shakhtarin.rdrone.server.graphic.level.CellType
import ru.pavel.shakhtarin.rdrone.server.graphic.level.World
import ru.pavel.shakhtarin.rdrone.server.graphic.model.ModelHandler
import ru.pavel.shakhtarin.rdrone.server.graphic.screen.MainScreen
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.CellId
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Seeker
import ru.pavel.shakhtarin.rdrone.server.rsocket.server.RServer
import ru.pavel.shakhtarin.rdrone.server.rsocket.server.RSocketImplementation
import ktx.app.KtxGame
import ktx.app.KtxScreen

class RDroneServerKTXMain(
    private val world: World
) : KtxGame<KtxScreen>() {

    private val cellRenderer: CellRenderer = CellRenderer()
    private lateinit var modelHandler: ModelHandler

    override fun create() {
        RServer(RSocketImplementation(world)).startServer()
        modelHandler = initModelHandler(world)
        addScreen(MainScreen(initCamera(world), modelHandler, world, cellRenderer))
        setScreen<MainScreen>()
    }

    private fun initCamera(world: World): PerspectiveCamera {
        val middle = (world.size * 2).toFloat()
        val cameraPoint = Vector3(middle, middle, middle)
        return PerspectiveCamera(47f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            position.set(cameraPoint)
            lookAt(cameraPoint)
            near = 1f
            far = 300f
            update()
        }
    }

    private fun createBox(builder: ModelBuilder, size: Float, color: ColorAttribute): Model {
        val attributes = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        return builder.createBox(size, size, size, Material(color), attributes)
    }

    @Suppress("SameParameterValue")
    private fun createLineGrid(builder: ModelBuilder, gridK: Int, size: Int, color: ColorAttribute): Model {
        val attributes = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        return builder.createLineGrid(
            size / gridK,
            size / gridK,
            gridK.toFloat(),
            gridK.toFloat(),
            Material(color),
            attributes
        )
    }

    private fun initModelHandler(world: World): ModelHandler {
        val floatSize = world.size.toFloat()
        val size = world.size
        val offset = floatSize / 2
        val inCellOffset = 0.5f
        val gridK = 1
        val supportPointSize = 0.3f
        val attributes = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        val modelBuilder = ModelBuilder()
        val zeroPoint = createBox(modelBuilder, supportPointSize, ColorAttribute.createDiffuse(Color.RED))
        val supportXPoint = createBox(modelBuilder, supportPointSize, ColorAttribute.createDiffuse(Color.PURPLE))
        val supportYPoint = createBox(modelBuilder, supportPointSize, ColorAttribute.createDiffuse(Color.BLUE))
        val supportZPoint = createBox(modelBuilder, supportPointSize, ColorAttribute.createDiffuse(Color.GOLD))
        val worldBox = createBox(
            modelBuilder,
            floatSize,
            ColorAttribute.createDiffuse(255f, 255f, 255f, 0.1f)
        )
        val worldBoxInstance = ModelInstance(worldBox).apply {
            transform.translate(Vector3(offset, offset, offset))
            materials.first().set(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
        }
        val zeroPointInstance = ModelInstance(zeroPoint)
        val supportXPointInstance = ModelInstance(supportXPoint).apply {
            transform.translate(Vector3(floatSize, 0f, 0f))
        }
        val supportYPointInstance = ModelInstance(supportYPoint).apply {
            transform.translate(Vector3(0f, floatSize, 0f))
        }
        val supportZPointInstance = ModelInstance(supportZPoint).apply {
            transform.translate(Vector3(0f, 0f, floatSize))
        }
        val grid = createLineGrid(
            modelBuilder,
            gridK,
            size,
            ColorAttribute.createDiffuse(255f, 255f, 255f, 0.1f)
        )
        val gridBottom = createLineGrid(
            modelBuilder,
            gridK,
            size,
            ColorAttribute.createDiffuse(255f, 255f, 255f, 0.2f)
        )
        val shellGrids = mutableListOf<ModelInstance>()
        val shellGridA = ModelInstance(gridBottom).apply {
            transform.translate(Vector3(offset, 0f, offset))
        }
        val shellGridB = ModelInstance(grid).apply {
            transform.translate(Vector3(offset, offset * 2, offset))
        }
        val shellGridC = ModelInstance(grid).apply {
            transform.rotate(Vector3(offset, 0f, 0f), 90f)
            transform.translate(Vector3(offset, 0f, -offset))
        }
        val shellGridD = ModelInstance(grid).apply {
            transform.rotate(Vector3(0f, 0f, offset), 90f)
            transform.translate(Vector3(offset, 0f, offset))
        }
        shellGrids += shellGridA
        shellGrids += shellGridC
        shellGrids += shellGridD
        val gridInstances = (0..size step gridK).fold(mutableListOf<ModelInstance>()) { acc, i ->
            acc += ModelInstance(grid).apply {
                transform.translate(Vector3(offset, i.toFloat(), offset))
                materials.first().set(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
            }
            acc += ModelInstance(grid).apply {
                transform.rotate(Vector3(offset, 0f, 0f), 90f)
                transform.translate(Vector3(offset, i.toFloat(), -offset))
                materials.first().set(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
            }
            acc
        }

        val seekerModels = world.seekers.fold(mutableListOf<Model>()) { acc, seeker ->
            acc += createBox(
                modelBuilder,
                1f,
                ColorAttribute.createDiffuse(seeker.color)
            )
            acc
        }
        val seekerInstances = mutableListOf<ModelInstance>()
        val visionModel = createBox(modelBuilder, 1f, ColorAttribute.createDiffuse(255f, 255f, 255f, 0.1f))
        world.seekers.forEachIndexed { i, seeker ->
            seekerInstances += ModelInstance(seekerModels[i]).apply {
                userData = CellId(seeker.id, seeker, CellType.SEEKER)
                val vector3D = seeker.currentPosition
                val vector =
                    Vector3(vector3D.x + 0.5f, vector3D.y + 0.5f, vector3D.z + 0.5f)
                transform.translate(vector)
            }
        }
        val allInstances = mutableListOf(
            zeroPointInstance,
            supportXPointInstance,
            supportYPointInstance,
            supportZPointInstance,
        ).apply {
            addAll(shellGrids)
            addAll(seekerInstances)
        }
        return ModelHandler(
            models = listOf(),
            instances = allInstances,
            seekerInstances = hashMapOf<Seeker, MutableList<ModelInstance>>(),
            modelBatch = ModelBatch(),
            modelBuilder = modelBuilder
        )
    }

}
