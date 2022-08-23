package ru.pavel.shakhtarin.rdrone.server.rsocket.server


import ru.pavel.shakhtarin.rdrone.server.extension.toObject
import ru.pavel.shakhtarin.rdrone.server.graphic.level.World
import ru.pavel.shakhtarin.rdrone.server.rsocket.data.InitRequest
import io.rsocket.Payload
import io.rsocket.RSocket
import org.slf4j.LoggerFactory

import reactor.core.publisher.Flux


class RSocketImplementation(private val world: World) : RSocket {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun requestStream(payload: Payload): Flux<Payload> {
        val initRequestId = payload.toObject<InitRequest>().id
        logger.info("Init request from client: $initRequestId")
        world.addSeeker(initRequestId)
        return Flux.create { emitter -> world.emitterMap[initRequestId] = emitter }
    }
}