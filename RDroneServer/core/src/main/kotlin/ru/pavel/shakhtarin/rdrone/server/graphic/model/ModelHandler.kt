package ru.pavel.shakhtarin.rdrone.server.graphic.model

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.Seeker

data class ModelHandler(
    val models: List<Model>,
    var instances: List<ModelInstance>,
    var seekerInstances: HashMap<Seeker, MutableList<ModelInstance>>,
    val modelBatch: ModelBatch,
    val modelBuilder: ModelBuilder
)
