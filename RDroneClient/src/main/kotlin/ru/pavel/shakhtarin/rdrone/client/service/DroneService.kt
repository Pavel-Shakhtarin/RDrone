package ru.pavel.shakhtarin.rdrone.client.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import ru.pavel.shakhtarin.rdrone.client.rsocket.RSocketClient

@Service
class DroneService(private val rSocketClient: RSocketClient) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun initDrones(amount: Int) {
        Flux.fromIterable((0 until amount))
            .flatMap { rSocketClient.initRSocket() }
            .flatMap { rSocket -> rSocket.requestStream(rSocketClient.initRequest()) }
//            .map { payload -> payload.toMonoObject<TargetData>() }
            .doOnNext { data -> logger.info("Received data: $data") }
            .doOnError { logger.error("Drone stream error: ${it.message}") }
            .subscribe()
    }

}