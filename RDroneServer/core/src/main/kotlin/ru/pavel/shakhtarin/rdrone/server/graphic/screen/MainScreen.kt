package ru.pavel.shakhtarin.rdrone.server.graphic.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector3
import ru.pavel.shakhtarin.rdrone.server.graphic.level.CellRenderer
import ru.pavel.shakhtarin.rdrone.server.graphic.model.ModelHandler
import ru.pavel.shakhtarin.rdrone.server.graphic.level.World
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.CellId
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Seeker
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import kotlin.math.abs
import kotlin.math.roundToInt

class MainScreen(
    private val camera: PerspectiveCamera,
    private val modelHandler: ModelHandler,
    private val world: World,
    private val cellRenderer: CellRenderer
) : KtxScreen {

    private var seekerUnderCamera = 0
    private var currentCameraPosition = Vector3(0f,0f,0f)

    override fun render(delta: Float) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        modelHandler.apply {
            modelBatch.begin(camera)
            modelBatch.render(instances)
            seekerInstances.values.forEach {
                modelBatch.render(it)
            }
            processSeeker(instances)
            modelBatch.end()
        }
        updateCamera()
        keyboardInput(currentCameraPosition)
    }

    override fun dispose() {
        modelHandler.models.disposeSafely()
        modelHandler.modelBatch.disposeSafely()
    }

    private fun processSeeker(instances: List<ModelInstance>) {
//        rotateTargets(instances.filter { it.userData == CellType.TARGET })
        instances
            .filter { it.userData != null }
            .filter { (it.userData as CellId).seeker != null }
            .forEach { startSeeker((it.userData as CellId).seeker!!, seekerInstance = it) }
    }

    private fun rotateTargets(targets: List<ModelInstance>) {
        val rotationRate = 15f
        targets.forEach {
            val position = it.transform.getTranslation(Vector3())
            it.transform.rotate(position.x, position.y, 0f, rotationRate)
        }
    }

    private fun startSeeker(seeker: Seeker, seekerInstance: ModelInstance) {
        if (!seeker.moving && seeker.path.isNotEmpty()) {
            adjustSeeker(seeker)
            seeker.moving = true
        }
        if (seeker.moving) moveSeeker(seeker, seekerInstance)
    }

    private fun adjustSeeker(seeker: Seeker) {
        seeker.currentPosition.x = seeker.currentPosition.x.roundToInt().toFloat()
        seeker.currentPosition.y = seeker.currentPosition.y.roundToInt().toFloat()
        seeker.currentPosition.z = seeker.currentPosition.z.roundToInt().toFloat()
        val vision = world.seekerVision(seeker.currentPosition)
        val next = seeker.path.poll()
        if (next != null) {
            seeker.nextStep = next
            val direction = Vector3(next.x, next.y, next.z)
            seeker.direction = direction.sub(seeker.currentPosition).nor()
            cellRenderer.reRenderCells(modelHandler, world, seeker, vision)
        } else {
            seeker.moving = false
            seeker.path = world.path(seeker.currentPosition, vision)
            seeker.visionMatrix = vision
        }
    }

    private fun moveSeeker(seeker: Seeker, seekerInstance: ModelInstance) {
        val threshold = seeker.movementK * 3
        val direction = seeker.direction
        val nextStep = seeker.nextStep
        val currentPosition = seeker.currentPosition
        if (abs(nextStep.dst(currentPosition)) <= threshold) {
            val offset =
                Vector3(
                    /* x = */ nextStep.x - currentPosition.x,
                    /* y = */ nextStep.y - currentPosition.y,
                    /* z = */ nextStep.z - currentPosition.z
                )
            seekerInstance.transform.translate(offset)
            val point = world.worldMatrix[nextStep.x.toInt()][nextStep.y.toInt()][nextStep.z.toInt()]
            if (point.event != null) {
                //TODO Send event
                point.event = null
            }
            adjustSeeker(seeker)
        } else {
            val step =
                Vector3(direction.x * seeker.movementK, direction.y * seeker.movementK, direction.z * seeker.movementK)
            seeker.currentPosition.add(step)
            seekerInstance.transform.translate(step)
        }
    }

    private fun updateCamera() {
        camera.lookAt(currentCameraPosition)
        camera.update()
    }

    private fun keyboardInput(position: Vector3) {
        val step = 1f
        val worldHalfSize = (world.size / 2).toFloat()
        //Vector3(worldHalfSize, worldHalfSize, worldHalfSize)
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.rotateAround(position, Vector3(worldHalfSize, 0f, 0f), step)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.rotateAround(position, Vector3(-worldHalfSize, 0f, 0f), step)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.rotateAround(position, Vector3(0f, worldHalfSize, 0f), step)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.rotateAround(position, Vector3(0f, -worldHalfSize, 0f), step)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.rotateAround(position, Vector3(0f, 0f, worldHalfSize), step)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.rotateAround(position, Vector3(0f, 0f, -worldHalfSize), step)
        }
        val cameraPosition = camera.position
        val zoomOut =
            Vector3(cameraPosition.x - position.x, cameraPosition.y - position.y, cameraPosition.z - position.z).nor()
        val zoomIn =
            Vector3(position.x - cameraPosition.x, position.y - cameraPosition.y, position.z - cameraPosition.z).nor()
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(zoomOut)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(zoomIn)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            if (world.seekers.isNotEmpty()) {
                val seeker = if (++seekerUnderCamera >= world.seekers.size) {
                    seekerUnderCamera = 0
                    0
                }
                else seekerUnderCamera
                val newCameraPosition = world.seekers[seeker].currentPosition
                currentCameraPosition = newCameraPosition
            }
        }
    }

}