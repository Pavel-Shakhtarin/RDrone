package ru.pavel.shakhtarin.rdrone.server.rsocket.server

import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.core.RSocketServer
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.transport.netty.server.WebsocketServerTransport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

class RServer(
    private val rSocketImplementation: RSocketImplementation
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private var rSocket: RSocket? = null
    private var thread: Thread? = null

    fun startServer() {
        thread = Thread {
            Hooks.onErrorDropped { logger.error("RSocket error: ${it.message}") }
            RSocketServer.create()
                .acceptor { setupPayload, rSocket -> handleSetup(setupPayload, rSocket) }
//                .resume(Resume().sessionDuration(Duration.ofHours(24)))
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .bind(WebsocketServerTransport.create(13666))
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext { logger.info("RSocket server started on: ${it.address()}") }
                .block()
                ?.onClose()
                ?.block()
        }
        thread?.start()
    }

    private fun handleSetup(setup: ConnectionSetupPayload, rSocket: RSocket): Mono<RSocket> {
        logger.info("RSocket setup: ${setup.dataUtf8}")
        this.rSocket = rSocket
        return rSocketImplementation.toMono()
    }

}