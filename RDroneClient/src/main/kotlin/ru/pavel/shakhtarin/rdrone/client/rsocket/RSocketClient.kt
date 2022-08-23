package ru.pavel.shakhtarin.rdrone.client.rsocket

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.CompositeByteBuf
import io.rsocket.RSocket
import io.rsocket.core.RSocketConnector
import io.rsocket.core.Resume
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.metadata.CompositeMetadataCodec
import io.rsocket.metadata.TaggingMetadataCodec
import io.rsocket.metadata.WellKnownMimeType
import io.rsocket.transport.netty.client.WebsocketClientTransport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import ru.pavel.shakhtarin.rdrone.client.extention.toPayload
import ru.pavel.shakhtarin.rdrone.client.rsocket.data.InitRequest
import java.net.URI
import java.time.Duration
import kotlin.random.Random

@Component
class RSocketClient {

    @Value("\${rsocket.server.address}")
    private val server: String = ""

    @Value("\${rsocket.server.port}")
    private val port: Int = 0

    private val ws = lazy { WebsocketClientTransport.create(server, port) }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        Hooks.onErrorDropped {
            logger.error("RSocket error: ${it.message}")
            it.printStackTrace()
        }
    }

    fun initRSocket(): Mono<RSocket> {
        return RSocketConnector.create()
            .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.string)
            .dataMimeType(WellKnownMimeType.APPLICATION_JSON.string)
            .payloadDecoder(PayloadDecoder.ZERO_COPY)
//            .resume(resume(retry()))
            .connect(ws.value)
            .doOnSuccess { logger.info("Connected") }
            .doOnError { logger.error("Connection error: ${it.message}") }
    }

    fun blockRSocket(): RSocket {
        return RSocketConnector.create()
            .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.string)
            .dataMimeType(WellKnownMimeType.APPLICATION_JSON.string)
            .payloadDecoder(PayloadDecoder.ZERO_COPY)
//            .resume(resume(retry()))
            .connect(ws.value)
            .doOnSuccess { logger.info("Connected") }
            .doOnError { logger.error("Connection error: ${it.message}") }
            .block()!!
    }

    private fun retry(): RetryBackoffSpec =
        Retry.backoff(Byte.MAX_VALUE.toLong(), Duration.ofMillis(500)).maxBackoff(Duration.ofMinutes(1))

    private fun resume(retry: Retry): Resume = Resume().retry(retry).sessionDuration(Duration.ofHours(24))

    fun initRequest() =
        InitRequest(id = System.currentTimeMillis() + Random.nextLong(0, Short.MAX_VALUE.toLong())).toPayload()

    private fun rSocketMetadata(): ByteArray {
        val compositeByteBuf = CompositeByteBuf(ByteBufAllocator.DEFAULT, false, 1)
        val routingMetadata = TaggingMetadataCodec.createRoutingMetadata(
            ByteBufAllocator.DEFAULT,
            listOf()
        )
        CompositeMetadataCodec.encodeAndAddMetadata(
            compositeByteBuf,
            ByteBufAllocator.DEFAULT,
            WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
            routingMetadata.content
        )
        return ByteBufUtil.getBytes(compositeByteBuf)
    }

}