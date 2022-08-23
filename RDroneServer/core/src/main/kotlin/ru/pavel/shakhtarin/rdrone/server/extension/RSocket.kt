package ru.pavel.shakhtarin.rdrone.server.extension

import ru.pavel.shakhtarin.rdrone.server.rsocket.data.RSocketData
import io.rsocket.Payload
import io.rsocket.util.DefaultPayload
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

private val logger: Logger = LoggerFactory.getLogger("Extension")

fun RSocketData.toPayload(): Payload = DefaultPayload.create(this.serialize())

inline fun <reified T : RSocketData> Payload.toMonoObject(): Mono<T> {
    return this.toObject<T>().toMono()
}

inline fun <reified T : RSocketData> Payload.toObject(): T {
    val buffer = this.data
    val arr = ByteArray(buffer.rewind().remaining())
    return arr.deserializeData()
}